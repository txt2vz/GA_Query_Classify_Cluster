package index

class ImportantWordsSpec extends spock.lang.Specification {

	def "importantWords F1 oil"() {
		setup:
		IndexInfo.instance.setPathToIndex('indexes/r10')
		IndexInfo.instance.setCategoryNumber('2')
		IndexInfo.instance.setIndex()
		ImportantWords iw = new ImportantWords()

		when:
		def oilList = iw.getF1WordList (false, true)

		then:
		oilList[0] == "oil"
	}

	def "ImportantWords 20News3 tfidf"	(){
		setup:
		IndexInfo.instance.setPathToIndex('indexes/20NG3SpaceHockeyChristian')
		IndexInfo.instance.setIndex()
		ImportantWords iw = new ImportantWords()

		when:
		def tfidfList = iw.getTFIDFWordList()

		then:
		tfidfList[0] == 'space'
		tfidfList[1] == 'god'
		tfidfList[4] == 'hockey'
	}
}