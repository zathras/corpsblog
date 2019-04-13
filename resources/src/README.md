This directory contains files used to create the resources in the generated
blog, that shouldn't be copied to it.  One is resource_list.txt, which
enumerates all the files to be copied.  This is needed because there's no
way to get a directory listing within a JAR.

Files in the src directory and its subdirectories will be not be copied
by corpsblog, even if they're mentioned in resource_list.txt.

The entire contents of the resources directory is copied into the
corpsblog distribution jar.  This includes src.
