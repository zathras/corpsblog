#!/home/billf/bin/kotlinc -script
/*
  Run with:
    ./make_resource_list.kts > ../resources/src/resource_list.txt
*/

import java.io.File

fun makeResourceList(dir : File, path : String) {
    if (path == "src/") {
	return;
    }
    for (f in dir.list()) {
	val ff = File(dir, f)
	if (ff.isDirectory) {
	    makeResourceList(ff, path + ff.getName() + "/")
	} else {
	    println(path + ff.getName())
	    println(ff.lastModified())
	}
    }
}

val d = File("../resources")
makeResourceList(d, "")
