/*
TODO:
	- click handlers for search/reset buttons?
*/

const DOCUMENTSAPI = 'rest/documents/searchText?text=';

// Load API data
function loadAPIData(url, callback) {
  'use strict';
  var xhr = new XMLHttpRequest();
  var data = '';
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
		var id=results[i].id;
		var categories=results[i].categories;
		var snippet = results[i].description;
		var pubdate=results[i].publishDate;
		var link = getResultLink(id);
		var $item = $('<a href="' + link + '" target="_blank" class="result"><h3>' + title + '</h3>' + '<p>' + snippet + '&hellip;</p><p>'+pubdate+'</p></a>').hide().fadeIn();
		$element.append($item);
	}
}

function getResultLink(id) {
	'use strict';
	
	var url = 'rest/documents/' + id;
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
		
	loadAPIData(DOCUMENTSAPI + keyword+dts, showResults);
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