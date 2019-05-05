# Decomparer
GUI to compare [java] decompiler results

This decompiles two different files and allows you to view the results, hightlighting changes in both the file tree and in the files you open.
The arguments are rather self-explainatory, but here is an example anyway:

`-d forgeflower.jar -lf remote.jar -rf local.jar -t temp -p`

Note that you can also decompile the same file with different decompilers:

`-ld fernflower.jar -rd fernflower-patched.jar -bf test.jar -p`
