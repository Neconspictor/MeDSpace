function appendToParent(parentNode, node) {
    var fragment = document.createElement('div');//document.createDocumentFragment();
    fragment.innerHTML = node;
    while (fragment.children.length > 0) {
    parentNode.appendChild(fragment.children[0]);
  }
}

function writeTextInDiv(parentNode, text) {
    var htmlCode = "<div>" + text + "</div>"
    appendToParent(parentNode, htmlCode);
}



function foo(elementID) {
	var element = document.getElementById(elementID);
    var elementSelector = document.querySelector("#" + elementID);
    var i, j; //loop iterators
    var classArray;
    var currentClass; //loop variable
    //element.innerHTML = "Paragraph changed.";
    elementSelector.classList.add("batman");
    elementSelector.classList.add("test");
    
    // get array of classes
    classArray = elementSelector.className.split(' ');
    for (i = 0; i < classArray.length; ++i) {
        currentClass = classArray[i];
        if (currentClass == "batman") {
            for (j = 0; j < 15; ++j) {
                    writeTextInDiv(element, "class batman found!");
            }
        }
    }
}