AnDCpp
======

An Android DC++ client based on libjdcpp


Repo Instructions
=================

The AnDCpp project uses code from the libjdcpp project as a git submodule. In order to compile AnDCpp you will need to initialize the libjdcpp submodules to access the library code. Please run "git submodule init" and "git submodule update" from within libjdcpp directory to setup the same on a new clone. (Refer : http://git-scm.com/book/en/Git-Tools-Submodules)


Build & Compile Instructions
============================

An auto generated ant build file is provided. Run "ant debug" to generate a debug binary apk for the project. Alternatively to build a release version specify keystore location in an ant.properties (NOT to be comitted) file pointing to the keystore location. You can also import the project directly in to eclipse with ADT installed and build from there.
