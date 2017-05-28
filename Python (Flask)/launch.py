from flask import Flask
from flask import request
from flask import render_template
import time
import p
import os
app = Flask(__name__)

@app.route('/search/<name>/')
def getRecipeDeets(name):
	s = str(p.getSearchForDish(name)).replace("'","")
	return s
@app.route('/search/<name>/<index>/')
def getRecipeMoreDeets(name,index):
	s = str(p.getMoreRecipes(name,index)).replace("'","")[1:-1]
	return s
@app.route("/sous/<num>/<dish>/")
def getDishDeets(num,dish):
	url = "http://allrecipes.com/recipe/"+str(num)+"/"+str(dish)+"/"
	return str(p.getData(url)).replace("'","")
if __name__ == '__main__':
	app.run()