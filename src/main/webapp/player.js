$(document).ready(function() {
    $.getJSON('http://localhost:8080/video-training/api/streamapp').success(initPlayer)
    $.getJSON('http://localhost:8080/video-training/api/list').success(renderPlaylist)
    $('#playlist').click(function (event){
        event.preventDefault()
        $f().play(event.target.href)
    })

    function initPlayer(json) {
        $f("player", "flowplayer/flowplayer-3.2.7.swf", {
            clip: {
                scaling : 'fit',
                autoPlay: false,
                url: "mp4:sample.mp4",
                provider : 'rtmp'
            },
            plugins : {
                rtmp : {
                    url : 'flowplayer.rtmp-3.2.3.swf',
                    netConnectionUrl: json.host

                },
                controls: {
                    border: "5px solid #cccccc",
                    url: 'flowplayer.controls-3.2.5.swf'
                }
            }
        })
    }

    function renderPlaylist(json) {
      $.each(json, function (index, elem){
          $('<li><img width="84" height="48" src="'+ elem.imageUrl+'"><a href="'+ elem.url+'"> '+ elem.id +' <br />'+elem.generationTime+'</a></li>').appendTo('#playlist')
      })
    }
})