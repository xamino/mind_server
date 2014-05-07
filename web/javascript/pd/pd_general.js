/*********************** public display - toggle settings **************************/

/**
 * toggle the slide effect on the public displays for the display settings
 */
function toggleDisplaySettings(){
	if($('#show_app_settings').css('display') != 'none'){	//check wether app settings are still open
		$( "#show_app_settings" ).toggle("slow", function() {
				$('#app_settings_img').css('background-image', 'url(../images/pd_icons/settings_default_350px.png)');
		});
	}
	$( "#show_display_settings" ).toggle("slow", function() {
		if($('#show_display_settings').css('display') != 'none'){
			$('#display_settings_img').css('background-image', 'url(../images/pd_icons/settings_clicked_350px.png)');
			$("#toggleFullscreen").css('position', 'absolute');
			$("#toggleFullscreen").css('right', '15px');
			var buttonwidth = $("#toggleFullscreen").width();
			$("#displayLogoutButton").css('position', 'absolute');
			$("#displayLogoutButton").css('right', (+buttonwidth + +50)+'px');
		}
		if($('#show_display_settings').css('display') != 'block'){
			$('#display_settings_img').css('background-image', 'url(../images/pd_icons/settings_default_350px.png)');
		}
	});

}

/**
 * toggle the slide effect on the public displays for the app settings
 */
function toggleAppSettings(){
	if($('#show_display_settings').css('display') != 'none'){ //check wether app settings are still open
		$( "#show_display_settings" ).toggle("slow", function() {
			$('#display_settings_img').css('background-image', 'url(../images/pd_icons/settings_default_350px.png)');
		});
	}
	$( "#show_app_settings" ).toggle("slow", function() {
		if($('#show_app_settings').css('display') != 'none'){
			$('#app_settings_img').css('background-image', 'url(../images/pd_icons/settings_clicked_350px.png)');
		}
		if($('#show_app_settings').css('display') != 'block'){
			$('#app_settings_img').css('background-image', 'url(../images/pd_icons/settings_default_350px.png)');
		}
	});

}