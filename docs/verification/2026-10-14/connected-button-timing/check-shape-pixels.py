from pathlib import Path
import json,subprocess,numpy as np,sys
mode=sys.argv[1];assert mode in ('light','dark')
p=Path(__file__).resolve().parent;v=p/(mode+'-shape-timing.mp4')
info=json.loads(subprocess.check_output(['ffprobe','-v','error','-select_streams','v:0','-show_entries','frame=best_effort_timestamp_time','-of','json',str(v)],text=True));t=np.array([float(f['best_effort_timestamp_time']) for f in info['frames']])
b=subprocess.check_output(['ffmpeg','-v','fatal','-i',str(v),'-vf','crop=22:42:150:194','-fps_mode','passthrough','-f','rawvideo','-pix_fmt','rgb24','pipe:1']);a=np.frombuffer(b,dtype=np.uint8).reshape((-1,42,22,3));assert len(a)==len(t)
patch=np.concatenate([a[:,:9],a[:,-9:]],axis=1)
scores=((patch[:,:,:,0]>150)&(patch[:,:,:,1]<130)&(patch[:,:,:,2]<130)).mean(axis=(1,2));base=float(scores[0]);mask=scores>base+.025
segments=[];start=None
for i,value in enumerate(mask):
 if value and start is None:start=i
 if start is not None and (not value or i==len(mask)-1):
  end=i if value else i-1
  if t[end]-t[start]>.06:segments.append((start,end))
  start=None
assert len(segments)>=3,segments
rows=[]
for name,(start,end) in zip(('quick_100ms','held_650ms','quick_repeat_100ms'),segments[:3]):
 peak=float(max(scores[start:end+1]));rows.append({'action':name,'first_changed_frame_s':round(float(t[start]),4),'last_changed_frame_s':round(float(t[end]),4),'max_pressed_corner_coverage':round(peak,4),'returned_to_rest':bool(scores[min(end+1,len(scores)-1)]<=base+.025)})
assert abs(rows[0]['max_pressed_corner_coverage']-rows[1]['max_pressed_corner_coverage'])<.03,rows
assert all(row['returned_to_rest'] for row in rows)
result={'variant':'releaseValidation','theme':mode,'baseline_corner_coverage':round(base,4),'same_pressed_shape_on_quick_and_held':True,'presses':rows};(p/(mode+'-shape-pixels.json')).write_text(json.dumps(result,indent=2)+'\n');print(json.dumps(result,indent=2))
