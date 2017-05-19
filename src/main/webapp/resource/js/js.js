//弹出框
function dialog(id,time,option,url) {
	dialogClose()
	$(".mask").width($(document).outerWidth(true));
	$(".mask").height($(document).outerHeight(true));
	
	$(".mask").show();
	$(id).show();
	
	var dw2 = $(id + " .dialog_content").outerWidth()/2;
	var dh2 = $(id + " .dialog_content").outerHeight()/2;
	$(id + " .dialog_content").css({"margin-left":-dw2+"px","margin-top":-dh2+"px"});
    $(".desc").html(option);

	//假如有时间参数time，会定时关闭弹出框
	if(time){
		setTimeout(dialogClose,time);
	}
	if(url){
		setTimeout(function(){dialogUrl(url);},time);
	}
}
function dialogUrl(url) {
	window.location.href = url;
}
function dialogClose() {
	$(".dialog").hide();
	$(".mask").hide();
}
function noClose(){
	if(event.stopPropagation)
		 event.stopPropagation();
	else
		 event.cancelBubble = true;
}