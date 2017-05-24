import urllib2
import re
import requests
import string
from bs4 import BeautifulSoup
from urllib2 import urlopen

def removeHtml(s):
	res = ""
	check = True
	for i in s:
		if check:
			if i == "<":
				check = False
			else:
				res = res+i
		else:
			if i ==">":
				check = True
	return res.strip()
def getIngredients(plain_text):
	pages = []
	i = plain_text.find('<ul class="checklist dropdownwrapper list-ingredients-1"')
	il = plain_text.find('<ul class="checklist dropdownwrapper list-ingredients-2"')
	plain_text = plain_text[i:plain_text.find("</ul>",il)]
	soup = BeautifulSoup(plain_text,"html.parser")
	for link in soup.findAll('span'):
		if 'itemprop="ingredients"' in str(link):
			pages.append(removeHtml(link)+";a")
	if len(pages) > 1:
		pages[len(pages) -1] = pages[len(pages) -1][:-2]
	return pages
def getTimes(plain_text):
	pages = []
	i = plain_text.find('<ul class="prepTime">')
	plain_text = plain_text[i:plain_text.find("</ul>",i)]
	soup = BeautifulSoup(plain_text,"html.parser")
	for link in soup.findAll('li'):
		s = str(link)
		if 'class="prepTime__item"' in s:
			pages.append(removeHtml(s))
	
	return pages[1:]
def getDirections(plain_text):
	pages = []
	i = plain_text.find('<ol class="list-numbers recipe-directions__list" itemprop="recipeInstructions">')
	plain_text = plain_text[i:plain_text.find("</ol>",i)]
	soup = BeautifulSoup(plain_text,"html.parser")
	for link in soup.findAll('li'):
		s = str(link)
		if 'class="step"' in s:
			pages.append(removeHtml(s)+";a")
	if len(pages) > 1:
		pages[len(pages) -1] = pages[len(pages) -1][:-2]
	return pages
def getDishTitle(s):
	i = s.find("<h3")
	return s[s.find(">",i)+1:s.find("<",i+1)].strip()

def getRecipes(dish):
	dish = dish.replace(" ","&20")
	url = "http://allrecipes.com/search/results/?wt="+dish+"&sort=re"
	pages = []
	source_code = requests.get(url) #Gets source Code
	plain_text = source_code.text
	soup = BeautifulSoup(plain_text,"html.parser")
	for link in soup.findAll('a'):
		s = str(link)
		if 'data-internal-referrer-link="hub recipe"' in s:
			if "/recipe/" in s:
				if "http://allrecipes.com"+str(link.get("href")) not in pages:
					pages.append("http://allrecipes.com"+str(link.get("href")))
	return pages
def getMoreRecipes(dish,index):
	dish = dish.replace(" ","&20")
	url = "http://allrecipes.com/search/results/?wt="+dish+"&sort=re&page="+str(index)
	pages = []
	source_code = requests.get(url) #Gets source Code
	plain_text = source_code.text
	soup = BeautifulSoup(plain_text,"html.parser")
	for link in soup.findAll('a'):
		s = str(link)
		if 'data-internal-referrer-link="hub recipe"' in s:
			if "/recipe/" in s:
				if "http://allrecipes.com"+str(link.get("href")) not in pages:
					pages.append("http://allrecipes.com"+str(link.get("href")))
	return pages
#print getMoreRecipes("burger","3")
def getServings(plain_text):
	i = plain_text.find('Original recipe yields')
	if i == -1:
		return "u???"
	#print i
	return plain_text[i+23:plain_text.find("servings",i)].strip()
def getCal(plain_text):
	i = plain_text.find("""<span class="calorie-count" ng-class="{'active': nutritionSection_showing}"><span>""")
	if i == -1:
		return "u???"
	return plain_text[plain_text.find("n>",i)+2:plain_text.find("</span>",i)].strip()
def getData(url):
	source_code = requests.get(url) #Gets source Code
	plain_text = source_code.text
	return[getIngredients(plain_text),getTimes(plain_text),getServings(plain_text),getCal(plain_text),getDirections(plain_text)]
def scrape(url):
	pages = []
	source_code = requests.get(url) #Gets source Code
	plain_text = source_code.text
	soup = BeautifulSoup(plain_text,"html.parser")
	for link in soup.findAll('a'):
		href = str(link.get("href")) #Gets the actual link
		if "bing" not in href and "go.microsoft.com" not in href and "http://www.freebase.com/" not in href:
			if href.startswith("http"):
				if href not in pages:
					pages.append(href)
	return pages

##print getServings("http://allrecipes.com/recipe/256264/banana-tempura/")
def getSearch(name):
	name = name.replace(" ","+")
	url = "http://www.bing.com/search?q="+name+"&qs=n&form=QBLH&sp=-1&pq="+name+"&sc=9-6&sk=&cvid=ACF31D8DC7A140AC8CF356A0F61E10A1"
	return scrape(url)
def scrapeVideo(url):
	pages = []
	source_code = requests.get(url) #Gets source Code
	plain_text = source_code.text
	soup = BeautifulSoup(plain_text,"html.parser")
	for link in soup.findAll('a'):
		href = str(link.get("href")) #Gets the actual link
		if href not in pages:
			pages.append(href)
	return pages
def getVideoSearch(name):
	name = name.replace(" ","+")
	url = "https://www.youtube.com/results?search_query="+name
	arr = scrapeVideo(url)
	results = []
	for i in arr:
		if "/watch" in i:
			if i not in results:
				if "http" not in i:
					results.append(i.replace("/watch?v=",""))
	return results
def getSearchForDish(term):
	return [getSearch(term+ " recipes"),getVideoSearch(term+" recipes"),getRecipes(term)]

##print u
##print getData(u)





