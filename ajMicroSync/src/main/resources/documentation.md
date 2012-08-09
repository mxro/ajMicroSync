<!-- one.upload https://u1.linnk.it/qc8sbw/usr/apps/ajFileSync/docs/ajMicroSync -->
ajMicroSync
==========

appjangle Micro Sync enables you to keep text files or parts of text files in sync between multiple locations.

appjangle Micro Sync is a Java Swing application built on the [appjangle](http://appjangle.com) platform.

You can download the latest executable below:

[appjangle Micro Sync 0.0.1](https://dl.dropbox.com/u/957046/onedb/apps/ajMicroSync-0.0.1-standalone.jar)

## Usage

In any plain text file, insert <!-- one.ignoreNext -->`<!-- one.createPublic [title] -->` statements as shown below.    
 
<!-- one.ignoreNext -->  
   
    Content not under version control.
    <!-- one.createPublic ajMicroSync -->   
 
    The content you want to keep under version control.
 
<!-- one.ignoreNext -->
    
    <!-- one.end -->    
    Content not under version control.    

Now start appjangle Micro Sync by double-clicking on the downloaded JAR file.Sign in with an appjangle account. 
(You can easily sign up for free on [appjangle.com/signup](http://appjangle.com)).

Drag and drop the file you have edit and or the folder in which your file is stored onto the file list in appjangle Micro Sync.

Hit [Synchronize Now].The marked contents of your text file should now be uploaded to your the appjangle cloud storage. 

The plain text file will have been altered, with the <!-- one.ignoreNext -->`<!-- one.upload [title] -->` statement changed to:    

<!-- one.ignoreNext -->

    Content not under version control.    
    <!-- one.upload https://u1.linnk.it/qc8sbw/usr/apps/ajFileSync/docs/ajMicroSync -->
    
    The content you want to keep under version control.
    
<!-- one.ignoreNext -->

    <!-- one.end -->    
    Content not under version control.    

appjangle Micro Sync will have determined a globally unique identity for the text snippet you have marked.

Any changes made to the text file, will from now on be uploaded to the cloud every time appjangle Micro Sync is run. 
<!-- one.end -->
