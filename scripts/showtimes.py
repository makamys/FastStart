import glob
import os
import sys
from forgelogutil import get_startup_time


basepath = None

if len(sys.argv) == 2:
	basepath = sys.argv[1]
else:
	sys.exit('''Usage: {} PATH

If PATH is a folder, it and its subfolders will be recursively searched
for .log files, and the startup time will be printed for each of them.

If PATH is a file, the startup time for it will be printed.

The log files have to be Forge debug logs (fml-client-latest.log)'''.format(sys.argv[0]))

def explore(dirpath, level):
	#import pdb;pdb.set_trace()
	if level > 8:
		return
	print("  " * level + (os.path.basename(dirpath) if level > 0 else dirpath))
	try:
		for subdir in next(os.walk(dirpath))[1]:
			explore(dirpath + "/" + subdir, level + 1)
	except StopIteration as e:
		pass # python is weird
	
	for logname in glob.glob(dirpath + "/*.log"):
		print("{}{}  {}".format("  " * (level + 1), get_startup_time(logname), os.path.basename(logname)))

if os.path.isdir(basepath):
	explore(basepath, 0)
elif os.path.isfile(basepath):
	print("  " + basepath + " " + get_startup_time(basepath))
else:
	sys.exit("Invalid file or directory: {}".format(basepath))
