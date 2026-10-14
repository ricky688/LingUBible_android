from pathlib import Path
import subprocess,json,numpy as np
p=Path(__file__).resolve().parent
def inspect(name):
 v=p/(name+'-motion.mp4')
 ts=json.loads(subprocess.check_output(['ffprobe','-v','error','-select_streams','v:0','-show_entries','frame=best_effort_timestamp_time','-of','json',str(v)],text=True))['frames']
 times=np.array([float(x['best_effort_timestamp_time']) for x in ts])
 raw=subprocess.check_output(['ffmpeg','-v','fatal','-i',str(v),'-vf','crop=400:56:40:185','-fps_mode','passthrough','-f','rawvideo','-pix_fmt','rgb24','pipe:1'])
 frames=np.frombuffer(raw,dtype=np.uint8).reshape((-1,56,400,3)).astype(float)
 assert len(frames)==len(times)
 end=int(np.flatnonzero(times<2.4)[-1])
 results=[]
 for label,(x0,x1) in {'old':(18,130),'new':(137,249)}.items():
  a=frames[:,10:50,x0:x1]
  yy,xx=np.mgrid[0:40,0:x1-x0];distance=np.maximum(abs(xx-(x1-x0)/2)/((x1-x0)/2),abs(yy-20)/20)
  first=a[0];last=a[end]
  c0=np.median(first[distance<.9],axis=0);c1=np.median(last[distance<.9],axis=0)
  mask=(np.linalg.norm(first-c0,axis=2)<12)&(np.linalg.norm(last-c1,axis=2)<12)
  delta=c1-c0;strength=np.sum((a-first)*delta,axis=3)/np.sum(delta*delta)
  # For both buttons, measure remaining primary color, independently of direction.
  primary=strength if label=='new' else 1-strength
  edges=mask&(distance>.75)&(distance<.9);center=mask&(distance<.55)
  print(name,label,'samples',edges.sum(),center.sum(),'colors',c0,c1)
  assert edges.sum()>15 and center.sum()>15
  result=np.array([np.median(x[edges])-np.median(x[center]) for x in primary])
  results.append(result)
 if name=='light':
  paired=np.minimum(*results)
  paired[times>times[end]]=-1
  best=int(np.argmax(paired))
  assert paired[best]>.12,('Missing paired perimeter fill',float(paired[best]))
  return {'paired_ring_frame_s':float(times[best]),'old_edge_minus_center_primary_fraction':round(float(results[0][best]),3),'new_edge_minus_center_primary_fraction':round(float(results[1][best]),3),'paired_direction_assertion_passed':True}
 else:
  max_difference=max(float(np.max(abs(x))) for x in results)
  assert max_difference<.12,('Unexpected dark spatial fill',max_difference)
  return {'max_edge_center_difference':round(max_difference,3),'uniform_crossfade_assertion_passed':True}
result={n:inspect(n) for n in ('light','dark')}
(p/'pixel-motion-check.json').write_text(json.dumps(result,indent=2)+'\n')
print(json.dumps(result,indent=2))
