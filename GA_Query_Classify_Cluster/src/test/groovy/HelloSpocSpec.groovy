import spock.lang.*

class HelloSpockSpec extends spock.lang.Specification {
	def "length of Spock's and his friends' names"() {
	  expect:
	  name.size() == length
  
	  where:
	  name     | length
	  "Spock"  | 5
	  "Kirk"   | 4
	  "Scotty" | 6
	}
	
	def "should return 2 from first element of list"() {
		given:
			List<Integer> list = new ArrayList<>()
		when:
			list.add(1)
		then:
			1 == list.get(0)
	}
  }