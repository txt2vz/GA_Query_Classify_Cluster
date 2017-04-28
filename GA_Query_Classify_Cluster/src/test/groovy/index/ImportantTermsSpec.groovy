package index

class ImportantTermsSpec extends spock.lang.Specification {

	def "importantTerms F1 oil"() {
		setup:
		IndexInfo.instance.setPathToIndex('indexes/R10')
		IndexInfo.instance.setCategoryNumber('2')
		IndexInfo.instance.setIndex()
		ImportantTerms impTerms = new ImportantTerms()

		when:
		def oilList = impTerms.getF1TermQueryList ()

		then:
		oilList[0].toString((IndexInfo.FIELD_CONTENTS)) == 'oil'
		oilList[3].getTerm().text() == 'petroleum'
	}
	
	def "importantTerms F1 20NG graphics"() {
		setup:
		IndexInfo.instance.setPathToIndex('indexes/NG20')
		IndexInfo.instance.setCategoryNumber('2')
		IndexInfo.instance.setIndex()
		ImportantTerms impTerms = new ImportantTerms()

		when:
		def graphicsList = impTerms.getF1TermQueryList ()

		then:
		graphicsList[0].toString((IndexInfo.FIELD_CONTENTS)) == 'windows'
		graphicsList[4].getTerm().text() == 'files'
	}

	def "ImportantTerms 20News3 tfidf"(){
		setup:
		IndexInfo.instance.setPathToIndex('indexes/20NG3SpaceHockeyChristianL6')
		IndexInfo.instance.setIndex()
		ImportantTerms impTerms = new ImportantTerms()

		when:
		def tfidfList = impTerms.getTFIDFTermQueryList()

		then:
		tfidfList[0].getTerm().text() == 'space'
		tfidfList[2].getTerm().text() == 'god'
		tfidfList[4].getTerm().text() == 'jesus'
	}
}