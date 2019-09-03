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

Unfortunately, the level of documentation is very low. Please, contact the author for more information on how to use the software if needed.
