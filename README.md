# TunnistinPalveluFast
Server version of the HeLI language identifier.
Language identifier based on HeLI, a Word-Based Backoff Method for Language Identification.

If you are using the identifier on scientific work, please refer to the following articles:

For the method:

@inproceedings{jauhiainen2016heli,
  title={Heli, a word-based backoff method for language identification},
  author={Jauhiainen, Tommi Sakari and Linden, Bo Krister Johan and Jauhiainen, Heidi Annika and others},
  booktitle={Proceedings of the Third Workshop on NLP for Similar Languages, Varieties and Dialects VarDial3, Osaka, Japan, December 12 2016},
  year={2016}
}

For a use case:

@inproceedings{jauhiainen2019wanca,
  title={Wanca in Korp: Text corpora for underresourced Uralic languages},
  author={Jauhiainen, Heidi and Jauhiainen, Tommi and Lind{\'e}n, Krister},
  booktitle={DATA AND HUMANITIES (RDHUM) 2019 CONFERENCE: DATA, METHODS AND TOOLS},
  pages={21},
  year={2019}
}

The HeLI identifier uses the Google guava library. You have to download it from: "https://github.com/google/guava" and add it to your classpath. The identifier has been tested only in a linux/unix environment.

Here are detailed instructions that you can try to follow and adapt to your own computing environment.

Download the zip file from GitHub:

wget https://github.com/tosaja/TunnistinPalveluFast/archive/master.zip

Unzip it:

unzip master.zip

Go to the folder containing the Java.file:

cd TunnistinPalveluFast-master/

Unzip the example language models for Finnish and Swedish:

unzip LanguageModels.zip 

Download the guava from maven.org:

wget https://repo1.maven.org/maven2/com/google/guava/guava/23.0/guava-23.0.jar

Compile the java file using the guava as part of the classpath:

javac -cp './guava-23.0.jar' TunnistinPalveluFast.java

Run the java program using the guava as part of the classpath:

java -cp '.:./guava-23.0.jar' TunnistinPalveluFast

Then the server prompts "Ready to accept queries." if everything went well. The port is set in code to be 8082. If you do not have access to it or you want to change it for some other reasong, you have to edit the java file and re-compile it.

Then you can access the service through for example using telnet for testing (from the same server):

telnet 127.0.0.1 8082

--
Trying 127.0.0.1...
Connected to 127.0.0.1.
Escape character is '^]'.
--

At this point telnet is waiting for you to enter a line of text followed by newline, so we type:

Tämä on suomea

And the server responds with the language code of the identified language:

--
fin
Connection closed by foreign host.
--

---

Unfortunately, the level of documentation is very low. Please, contact the author for more information on how to use the software if the previous steps do not work.
