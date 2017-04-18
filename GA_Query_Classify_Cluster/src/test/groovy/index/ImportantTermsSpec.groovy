package index

class ImportantTermsSpec extends spock.lang.Specification {

	def "importantTerms F1 oil"() {
		setup:
		IndexInfo.instance.setPathToIndex('indexes/r10L6')
		IndexInfo.instance.setCategoryNumber('2')
		IndexInfo.instance.setIndex()
		ImportantTerms iw = new ImportantTerms()

		when:
		def oilList = iw.getF1TermQueryList ()

		then:
		oilList[0].toString((IndexInfo.FIELD_CONTENTS)) == "oil"
		oilList[3].getTerm().text() == 'petroleum'
	}

	def "ImportantTerms 20News3 tfidf"	(){
		setup:
		IndexInfo.instance.setPathToIndex('indexes/20NG3SpaceHockeyChristianL6')
		IndexInfo.instance.setIndex()
		ImportantTerms iw = new ImportantTerms()

		when:
		def tfidfList = iw.getTFIDFTermQueryList()

		then:
		tfidfList[0].getTerm().text() == 'space'
		tfidfList[2].getTerm().text() == 'god'
		tfidfList[4].getTerm().text() == 'jesus'
	}
}