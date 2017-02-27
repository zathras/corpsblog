This directory contains files used to create the resources in the output,
but that shouldn't directly be copied.  One is resource_list.txt, which
enumerates all the files to be copied -- that's needed because there's no
way to get a directory listing within a JAR.

Files in the src directory and its subdirectories will be not be copied,
even if they're mentioned in resource_list.txt.
