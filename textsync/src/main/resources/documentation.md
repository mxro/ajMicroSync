<!-- one.upload https://u1.linnk.it/qc8sbw/usr/apps/ajFileSync/docs/ajMicroSync -->
ajMicroSync
==========

appjangle MicroSync enables you to keep text files or parts of text files in sync between multiple locations.

appjangle MicroSync is a Java Swing application built on the [appjangle](http://appjangle.com) platform.

You can download the latest executable below:

[appjangle MicroSync 0.0.4](https://dl.dropbox.com/u/957046/onedb/mvn-releases/aj/apps/microsync/ajMicroSync/0.0.4/ajMicroSync-0.0.4-jar-with-dependencies.jar)

Or grab the source from github:

[appjangle MicroSync github repository](https://github.com/mxro/ajMicroSync)

## Changelog

[To Do](http://slicnet.com/mxrogm/mxrogm/apps/edit/docs/3/doc)

### New in 0.0.4

- Fixed a bug that only _un_modified nodes would be uploaded
- Improved stability by creating a new client context of each synchronization session
- Improved stability of regular expression evaluation (there was an infinite loop 
when there are comments with unmatched ends, see 
[Regular Expression to Find HTML Comments in Java](http://maxrohde.com/2012/08/17/regular-expression-to-find-html-comments-in-java/))
- made logging less verbose and more expressive

### New in 0.0.3

- Upload operation will only be performed for files, which are modified on the local disc.
- Saving a login will not persist email and password any longer but only a session id.

### New in 0.0.2

- changed names of operations 
- added `one.download` operation
- added `one.ignoreNext` operation

## Usage

In any plain text file, insert <!-- one.ignoreNext -->`<!-- one.createPublic [title] -->` statements as shown below.    
 
<!-- one.ignoreNext -->  
   
    Content not under version control.
    <!-- one.createPublic ajMicroSync -->   
 
    The content you want to keep under version control.
 
<!-- one.ignoreNext -->
    
    <!-- one.end -->    
    Content not under version control.    

Now start appjangle MicroSync by double-clicking on the downloaded JAR file.Sign in with an appjangle account. 
(You can easily sign up for free on [appjangle.com/signup](http://appjangle.com)).

Drag and drop the file you have edited and/or the folder in which your file is stored onto the file list in appjangle MicroSync.

Hit [Synchronize Now].

The marked contents of your text file should now be uploaded to your the appjangle cloud storage. 

The plain text file will have been altered, with the <!-- one.ignoreNext -->`<!-- one.createPublic [title] -->` statement changed to:    

<!-- one.ignoreNext -->

    Content not under version control.    
    <!-- one.upload https://u1.linnk.it/qc8sbw/usr/apps/ajFileSync/docs/ajMicroSync -->
    
    The content you want to keep under version control.
    
<!-- one.ignoreNext -->

    <!-- one.end -->    
    Content not under version control.    

appjangle MicroSync will have determined a globally unique identity for the text snippet you have marked.

Any changes made to the text file, will from now on be uploaded to the cloud every time appjangle MicroSync is run.

## Further Operations

Apart from the operations `one.createPublic` and `one.upload` the following operations are supported:

### one.download

Replacing any `one.upload` statement in your text files with `one.download` will,
instead of uploading any changes to the local file to the cloud, *download* any
changes made to the file on other locations.

<!-- one.ignoreNext -->

    <!-- one.download https://u1.linnk.it/qc8sbw/usr/apps/ajFileSync/docs/ajMicroSync -->
    
    [Here content will appear]

<!-- one.ignoreNext -->

    <!-- one.end -->

### one.create

The operation `one.create` works very similar to the already described operation `one.createPublic`.
Replacing `one.createPublic` with `one.create` in the example above will assure that
the node can only be *read* and *written* by the appjangle user, which has created the node.

`one.createPublic` will create nodes on the appjangle platform, which have *public read* and
can be written/changed only by the specified appjangle user (or other users the node has
been explicitly shared with).

### one.ignoreNext

This is really just a handy utility function to write MicroSync documentation (such as
this document). Adding <!-- one.ignoreNext -->`<!-- one.ignoreNext -->` *before* another
operation declaration will lead to this operation being ignored.

See the [source code of this document](http://u1.linnk.it/qc8sbw/usr/apps/ajFileSync/docs/ajMicroSync.value.md) for example usages.

<!-- one.end -->
