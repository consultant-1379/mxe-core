function showLegalNotice() {

	var legalNotice = document.getElementById("legal_notice");
	const legalNoticePath = legalNotice.getAttribute("for");
	const legalNoticeMsg = "/res/legal_notice.txt?v=" + new Date().getTime();
	const div = document.createElement('div');

	var legalNoticeReq = new XMLHttpRequest();

	legalNoticeReq.onreadystatechange = function() {
		if (this.readyState == 4 && this.status == 200) {

			var msg = legalNoticeReq.responseText;

			if(msg){
				// setup cleanup
				const l_button = document.createElement('span');

				legalNotice.setAttribute('class', 'eric-legal-notice');
				l_button.setAttribute('class', 'eric-confirm-button');
				l_button.setAttribute('onclick', 'removeLegalNotice()');
				l_button.innerText = 'Close';

				div.innerText = msg;
				
				//setup body gray
				document.getElementById("kc-header").setAttribute('style','opacity: 0.4;');
				
				
				legalNotice.appendChild(div);
				legalNotice.appendChild(l_button);
			}
			else {
				legalNotice.remove();
			}
		}

	};

	legalNoticeReq.open('GET', window.location.protocol + "//"
			+ window.location.host + legalNoticePath + legalNoticeMsg);
	legalNoticeReq.send();

}

function removeLegalNotice() {
	while (document.getElementById("legal_notice").firstChild) {
		document.getElementById("legal_notice").removeChild(
				document.getElementById("legal_notice").lastChild);
	}
	document.getElementById("legal_notice").removeAttribute("class");
	//setup body gray
	document.getElementById("kc-header").removeAttribute("style");
}


function swapLabelAnimateUsername () {
  document.getElementById("form_login_username_label").setAttribute("class","active");
  document.getElementById("username").removeAttribute("class");
  document.getElementById("username").setAttribute("class","form-control-active ");
}

function changeFocusOutLabelUsername() {

 if ( !document.getElementById("username").value ) {
  if (document.getElementById("form_login_username_label").hasAttribute("class") ) {
      document.getElementById("form_login_username_label").removeAttribute("class");
      document.getElementById("username").removeAttribute("class"); 
      document.getElementById("username").setAttribute("class","form-control");
  }
 }
 
}

function swapLabelAnimatePassword() {
     document.getElementById("form_login_password_label").removeAttribute("class");
     document.getElementById("password").removeAttribute("class");
     document.getElementById("form_login_password_label").setAttribute("class","active");
     document.getElementById("password").setAttribute("class","form-control-active ");
}

function changeFocusOutLabelPassword() {

 if ( !document.getElementById("password").value ) {
  if (document.getElementById("form_login_password_label").hasAttribute("class") ) {
      document.getElementById("form_login_password_label").removeAttribute("class");
      document.getElementById("password").removeAttribute("class");
      document.getElementById("form_login_password_label").setAttribute("class","normal_q"); 
      document.getElementById("password").setAttribute("class","form-control ");
  }
 }
 
}

