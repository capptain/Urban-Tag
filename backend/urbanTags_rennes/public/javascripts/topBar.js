$(function(){
	$('#loginButton').click(login)
});

function login()
{	
	$('#loginDiv .popup-content').load( getLoginFormAction(),
	   function() {
        $('#loginDiv').css('z-index', 10000)
        $('#loginDiv').fadeIn(300)
        $('#loginDiv .popup-main-container').center()
       }
	)
}