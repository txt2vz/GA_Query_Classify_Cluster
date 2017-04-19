
def docsPath = /C:\Users\Laurie\Dataset\20bydate/
def  fpath = /C:\Users\Laurie\Dataset\20bydate\20news-bydate-test\alt.atheism\53068/

File f = new File(fpath)

println f.canonicalPath

println f.canonicalPath.contains("test")


println "filename " + f.name

println f.name.contains("06")
//f.getText()