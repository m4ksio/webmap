#Webmap

Simple web crawler which generates graph describing a website.
Written using Scala, Akka, Akka HTTP, Scala-graph, tested using ScalaMock and ScalaTest.
Build by SBT

##Tradeoffs

Crawling is by design limited to one domain, external links are shown, but not traversed. Some edge cases of redirects may not work properly.
Maximum reponse time is 2 seconds, maximum crawling time for entire system is 1 minute.

## Building and running

    sbt assembly
    
This will create uberJAR which cam be run like 

    java -jar java -jar target/scala-2.12/webmap-assembly-1.0.jar http://localhost/ out.gv
    
Two required parameters are:
 * start URL - the starting URL for a crawler
 * output file - the file the resulting Graphviz/DOT file will be saved to
 
## Output

Graphviz/DOT format should be supported on all platforms. Having GraphViz suite installed, commnad
 
     dot -Tpng  web.gv > graph.png
     
generates PNG file with a graph