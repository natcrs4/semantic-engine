/*
TODO:
	- click handlers for search/reset buttons?
*/

const DOCUMENTSAPI = 'rest/documents/advancedsearch?classify=true&text=';
var taxonomy;
loadAPIData("rest/taxonomy/root/category/branch/root",callbackTaxonomy);
var parents;
var start=0;
var  maxresults=10;

// Load API data
function loadAPIData(url, callback) {
  'use strict';
  var xhr = new XMLHttpRequest();
  var data = '';
  var result;
  xhr.open("GET", url, true);
  xhr.send();
  xhr.onreadystatechange = function() {
    console.log(xhr.readyState + " " + xhr.status);
    if (xhr.readyState == 4 && xhr.status == 200) {
      data = xhr.responseText;
      callback(data);
    }
  };

}

function showResults(data) {
	'use strict';
	var parsed = JSON.parse(data);
	var results = parsed.documents;
	var $element = $(document.getElementById('results'));
	$element.html('');
	$('form').removeClass('centered');
	for (var i = 0; i < results.length; i++) {
		var title = results[i].title;
		var id=results[i].internal_id;
		var categories=results[i].categories;
		var snippet = results[i].description;
		var pubdate=results[i].publishDate;
		var link = getResultLink(id);
		var menu=createSelectionMenu(taxonomy,categories,id)
		
		var $item = $('<a href="' + link +"?classify=true"+ '" target="_blank" class="result"><h3>' + title + '</h3>' + '<p>' + 
				snippet + '&hellip;</p><p>'+pubdate+'</p></a>').hide().fadeIn();
		
		$element.append($item);
		$element.append($(menu));
		$('#'+id).on('change',function(){
			var url='rest/taxonomy/root/document';
			putDocumentToCategory(url,this.value,this.id);	
		});
	}
}

function putDocumentToCategory(url,category_id,document_id){
	

	
    if(category_id=='------') return;
	var xhr = new XMLHttpRequest();
	xhr.open("PUT", url+"/"+category_id, true);
	xhr.setRequestHeader('Content-type','text/plain; charset=utf-8');
	xhr.onload = function () {
		var text = xhr.responseText;
		if (xhr.readyState == 4 && xhr.status == "200") {
			console.table(text);
		} else {
			console.error(text);
		}
	}
	xhr.send(document_id);
}
function callbackTaxonomy(data){
	var parsed = JSON.parse(data);
	

	taxonomy=parsed;
	//parents=//return result;
}
function level1categories(taxonomy){
	var branchlist=new Array();
	branchlist[0]="------";
	for(var i=0;i<taxonomy.children.length;i++)
	branchlist[i+1]=taxonomy.children[i].name;
	
	return branchlist;
}

function createSelectionMenu(taxonomy, selected,id){
	var selected_first;
	if(selected_first)
		selected=selected[0];
	var set_selected = new Set();
	var branchlist=level1categories(taxonomy);
	var result='';
	for(var k=0;k<selected.length;k++){
		
		
		
		selected_first=selected[k];
		var pr=parent(taxonomy,selected_first);
	if(!set_selected.has(selected_first)){
		set_selected.add(selected_first)
	result=result+'<p>';
	
	
	var aux='<select id="'+id+'">';	
	for( var i=0;i<branchlist.length;i++)
		{
		 
         if(branchlist[i]==pr)
             aux = aux+ '<option value="'+branchlist[i]+'" selected>'+branchlist[i]+'</option>\n';
         else
             aux = aux+ '<option value="'+branchlist[i]+'">'+branchlist[i]+'</option>\n';
 
		}
     
       aux =aux+ '<select>';
       result+=aux+'<p>';
       
	}
	
	}
	result+='<button id=A'+id+'>add category</button>'
     return result;
}


function parent(taxonomy, category){
	for(var i=0;i<taxonomy.children.length;i++)
		{
		 var aux=taxonomy.children[i];
		 if(aux.name==category) return category;
		 for(var j=0;j<aux.children.length;j++){
			 if(aux.children[j].name==category)
				 return aux.name;
		    }
		 }
	return "------";
		
}

function getResultLink(id) {
	'use strict';
	
	var url = 'rest/documents/internal/' + id;
	return url;
}

function searchDocuments() {
	'use strict';
	var $input = document.getElementById('search');
	var keyword = $input.value;
	
	var $todate = document.getElementById('todate');
	var to_date = $todate.value;
	var $fromdate = document.getElementById('fromdate');
	var from_date = $fromdate.value;
	var  dts='';
	if(from_date)
		dts+='&from='+from_date;
	if(from_date)
		dts+='&to='+to_date;
	var pagination='&start='+start+'&maxresults='+maxresults;
		
	loadAPIData(DOCUMENTSAPI + keyword+dts+pagination, showResults);
	$input.blur();
}

function dayFinder() {

	  var userDate = document.getElementById("date").value;
	  var dateSplit =  userDate.split("-");
	  var yyyy = dateSplit[0];
	  var mm = dateSplit[1];
	  var dd = dateSplit[2];
	  var finalDate = new Date(mm +"/"+ dd +"/"+ yyyy); //new Date("06/03/2017");
	  var getDay = finalDate.getDay();
	  var daysArray = ["Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"];
	  document.getElementById("demo").innerHTML = daysArray[getDay];
	}

$( "select" ) .change(function () {    
	var vl=this.value;  
	alert(" this value"+ vl);
	});  
const c = document.querySelector('.container')
const indexs = Array.from(document.querySelectorAll('.index'))
let cur = -1
indexs.forEach((index, i) => {
  index.addEventListener('click', (e) => {
    // clear
    c.className = 'container'
    void c.offsetWidth; // Reflow
    c.classList.add('open')
    c.classList.add(`i${i + 1}`)
    start=i*10;
    searchDocuments();
    if (cur > i) {
      c.classList.add('flip')
    }
    cur = i
  })
})
