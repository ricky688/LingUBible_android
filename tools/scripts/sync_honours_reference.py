"""Copy the parent web app's bundled honours cohorts into an Android JSON asset."""
import json
import re
from pathlib import Path

root = Path(__file__).resolve().parents[2]
source = root.parent / 'src/data/firstClassHonours.ts'
text = source.read_text()
pattern = r'en: "([^"\n]+)",\s*tc: "([^"\n]+)",\s*sc: "[^"\n]+",\s*years: \{(.*?)\n    \},'

def years(body):
    result = {}
    for year, total, first, pct in re.findall(r'(\d{4}): \{ total: (null|[\d.]+), first: (null|[\d.]+), pct: (null|[\d.]+) \}', body):
        result[year] = {'total': json.loads(total), 'first': json.loads(first), 'pct': json.loads(pct)}
    return result

faculty = dict(re.findall(r'"([^"]+)": \'(\w+)\'', text.split('export const PROGRAMME_FACULTY:')[1]))
programmes = [{'en': en, 'tc': tc, 'faculty': faculty[en], 'years': years(body)} for en, tc, body in re.findall(pattern, text, re.S)]
assert len(programmes) == len(faculty), 'Incomplete programme extraction'
summary = years(text.split('export const HONOURS_SUMMARY:')[1])
assert summary and all(p['years'] for p in programmes)
asset = root / 'app/src/main/assets/data/first_class_honours.json'
asset.write_text(json.dumps({'programmes': programmes, 'summary': summary}, ensure_ascii=False, indent=2) + '\n')
print(f'Synced {len(programmes)} programmes, cohorts {list(summary)}')
