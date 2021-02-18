## Highspot Coding Challenge

Code submission for Highspot

### Requirements

    1. Ingest mixtape.jsonanda changesfile.  
    2. The changesfile should include multiple changes in a single file:
        * Add a new playlist; the playlist should contain at least one song.
        * Remove a playlist.
        * Add an existing song to an existing playlist.
    3. Output output.json using the same structure as mixtape.json with the changes applied.
    4. Add a README describing how you would scale this application to handle very large input files and/or very large changes files.
    
### How to Build/Run

You first must create a shadow jar file. To build a shadow jar run `./gradlew shadowJar`. This will create a `coding-challenge-shadow.jar` file in the `build/libs` directory.

The application requires two inputs in the following order
    
   1. A `changes.json` file 
   2. A `mixtape.json` file 
  
To run the application after build run: `java -jar ./build/libs/coding-challenge-shadow.jar <path to changes.json> <path to mixtape.json>`

I have examples of these files in `/src/test/resources` e.g.: `java -jar ./build/libs/coding-challenge-shadow.jar src/test/resources/changes.json src/test/resources/mixtape.json`

This project is also set up to run in intellij IDE. You can configure a run configuration by simply running `./gradlew idea`, and then going to `Edit Configurations` and adding the `Application` it created. See `build.gradle` for this setup.

In order to run this on large files it would require giving the JVM some extra memory; In my testing I ran with a 1GB heap, but since everything I do is in memory very large files would cause memory issues. 

### Assumptions

1. The only operations that the changes.json file contained were the ones outlined in the requirements.
2. This could be run on a machine that has access to a lot of memory. All the operations performed in this application rely on keeping java POJOs around for some amount of time, which can be expensive.
3. I didn't do any validation on the contents of the `changes.json` file. E.g.: if it told me to remove a playlist with a given id, but that playlist was never in `mixtape.json` I just skipped the instruction.

### Basic Arch of Application

The application runs based on command line input from the user and calls two main methods in the `Paser.java` class:
    
   1. `parser.parseAndStoreChanges(InputStream in)` which takes the contents of `changes.json` and utilizes a custom Jackson de-serializer to store in-memory the batch change instructions
   2. `parser.parseAndUpdate(Inputstream in)` which takes the contents of `mixtape.json` and attempts to both parse and write output at the same time using the Jackson stream API

### How could this scale? 

The age-old question...

My solution has a few issues that would scare me if we put this into production:
    
   1. It loads the files first into memory by using a `StringBuilder`, which could easily be problematic for VERY large JSON files.  
   2. The parsing creates a bunch of POJOs, which are expensive to keep around, and could cause the application to blow up if some clever fellow decided to feed it a multi gig JSON file.
   3. The problem is nice in that you really don't have to parse the entire `mixtape.json` file seeing as you only need to modify part of the contents. A person who is savvier with Jackson would have probably been able to figure out how to do that, but it didn't seem possible given that I had to re-write the JSON contents back out. Instead, I tried to read every token in `mixtape.json` and then just write back out that token to the output stream on the fly. That way the only expensive parsing I would have to do would be in the playlist section of `mixtape.json`. I wasn't able to get this to work before I ran out of time.  
   
How I would solve these things if I had a lot more time:

   1. There's not a ton we can do about this without knowing the constraints on the sizes of both the files. For example, if we know for certain that `mixtape.json` will never be larger than a GB then we can easily allocate off-heap buffers to load the contents into (assuming we have access to that much memory... but in the cloud memory is free, right??). Moving large amounts of data off-heap helps us control how much time we have to spend in GC, and can protect us from OOMs; if we see files that are larger than our constraints we can simply toss them.
   2. If given more time I would've utilized more of the Jackson stream API for parsing (I am using a mix of both that an the `ObjectMapper` because it was easier). I would want to avoid storing any POJOs at all and simply apply batch changes as I see them come in on the parser by simply writing the correct token to the OutputStream. I basically do this in my solution, but I create POJOs along the way. (see my rambling in #3 above)
   3. Figure out a way to only read the contents of `mixtape.json` that I need to - we can skip HUGE parts of the file seeing as we only really need to modify one part of the JSON. I still am not totally sure how this would work. I am pretty sure this isn't possible with Jackson, and would cause a headache when you try to write the new contents. 