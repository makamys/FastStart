

def timestamp2secs(timestamp):
	components = [int(x) for x in timestamp.split(":")]
	
	secs = components[2] + components[1] * 60 + components[0] * 60 * 60
	
	return secs
	
def secs2timestamp(secs):
	if secs == None:
		return "[ERROR]"
		
	components = [int(secs / 60 / 60), int(secs / 60 % 60), int(secs % 60)]
	val = ":".join(["{:02}".format(x) for x in components])
	
	return val

def get_startup_secs(logpath):
	start = None
	end = None
		
	for line in open(logpath, "r", encoding="utf8"):	
		if start == None and "Injecting tracing printstreams for" in line:
			start = line.split(" ")[0]
		elif "OpenAL initialized." in line:
			end = line.split(" ")[0]
	
	if end == None or start == None:
		#print("weirdness in",logpath,"(start =",start,"end =",end,")")
		return None
	else:
		return timestamp2secs(end[1:-1]) - timestamp2secs(start[1:-1])
		
def get_startup_time(logpath):
	try:
		return secs2timestamp(get_startup_secs(logpath))
	except Exception as e:
		return "[ERROR]"
