import subprocess
import collections

output = subprocess.check_output(['python', 'refactor_map.py']).decode('utf-8')
lines = [l.strip() for l in output.split('\n') if '->' in l]

destinations = collections.defaultdict(list)
for line in lines:
    src, dst = line.split(' -> ')
    destinations[dst].append(src)

for dst, srcs in destinations.items():
    if len(srcs) > 1:
        print(f"DUPLICATE DESTINATION: {dst} <- {srcs}")
