"""Run with the first-launch guide at the top, in a disposable Waydroid user.
Usage: python3 check-guide-boundary.py DEVICE QA_USER OUTPUT_DIR [--baseline] [--bottom]
Verified via ARTEMIS trace 76c61b94-98c3-4269-9f63-2299afb1f902.
"""
import json
import re
import subprocess
import sys
import time
from pathlib import Path

serial, qa_user, output = sys.argv[1:4]
output = Path(output)
output.mkdir(parents=True, exist_ok=True)
adb = ['adb', '-s', serial]
assert qa_user != '0', 'Use an isolated test user, not the owner account.'
assert subprocess.check_output(adb + ['shell', 'am', 'get-current-user'], text=True).strip() == qa_user

def nodes(value):
    if isinstance(value, dict):
        yield value
        for item in value.values():
            yield from nodes(item)
    elif isinstance(value, list):
        for item in value:
            yield from nodes(item)

def snapshot(name, allow_closed=False):
    path = output / (name + '.json')
    deadline = time.monotonic() + 10
    while time.monotonic() < deadline:
        subprocess.run(['android', 'layout', '--device', serial, '--no-idle', '--full', '--output', str(path)], check=True, stdout=subprocess.DEVNULL)
        items = list(nodes(json.loads(path.read_text())))
        titles = [n for n in items if n.get('text') in ('入門指南 Getting started', 'Getting started')]
        if titles:
            next_button = next((n for n in items if n.get('text') in ('下一步 Next', 'Next')), None)
            center_y = lambda n: int(re.findall(r'\d+', n['center'])[1]) if n and 'center' in n else None
            return items, {'header_y': center_y(titles[0]), 'next_y': center_y(next_button)}
        if allow_closed:
            return items, {'header_y': None, 'next_y': None}
        time.sleep(.2)
    raise AssertionError('First-launch guide not visible; open it before running this check.')

time.sleep(1)
items, before = snapshot('before')
try:
    scroll = max((n for n in items if 'SCROLLABLE' in n.get('interactions', [])), key=lambda n: int(re.findall(r'\d+', n['bounds'])[3]) - int(re.findall(r'\d+', n['bounds'])[1]))
    left, top, right, bottom = map(int, re.findall(r'\d+', scroll['bounds']))
except (ValueError, KeyError):
    # Coordinate fallback verified on this 480x1000 Waydroid viewport.
    assert '480x1000' in subprocess.check_output(adb + ['shell', 'wm', 'size'], text=True)
    left, top, right, bottom = 0, 162, 480, 863
x, y = (left + right) // 2, top + (bottom - top) // 2
record = subprocess.Popen(adb + ['shell', 'screenrecord', '--bit-rate', '2500000', '--time-limit', '8', '/sdcard/Download/lingu-guide-boundary-check.mp4'])
time.sleep(.7)
drag = subprocess.Popen(adb + ['shell', 'input', 'swipe', str(x), str(y), str(x), str(y + (-160 if '--bottom' in sys.argv else 160)), '3200'])
time.sleep(1.3)
_, during = snapshot('during')
drag.wait(timeout=6)
time.sleep(1)
_, after = snapshot('after', allow_closed='--baseline' in sys.argv)
record.wait(timeout=12)
subprocess.run(adb + ['pull', '/sdcard/Download/lingu-guide-boundary-check.mp4', str(output / 'boundary.mp4')], check=True, stdout=subprocess.DEVNULL)
subprocess.run(adb + ['shell', 'rm', '-f', '/sdcard/Download/lingu-guide-boundary-check.mp4'], check=True)
result = {'before': before, 'during': during, 'after': after, 'header_movement_px': abs(during['header_y'] - before['header_y'])}
(output / 'result.json').write_text(json.dumps(result, indent=2) + '\n')
if '--baseline' in sys.argv:
    assert result['header_movement_px'] >= 8, result
else:
    assert max(v['header_y'] for v in (before, during, after)) - min(v['header_y'] for v in (before, during, after)) <= 2, result
    assert before['next_y'] is not None and before['next_y'] == during['next_y'] == after['next_y'], result
print(json.dumps(result))
