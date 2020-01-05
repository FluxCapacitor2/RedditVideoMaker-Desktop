var selection = [];
var toEval = '';
document.querySelector('link[title="applied_subreddit_stylesheet"]').remove();
window.onload = function () {
    //document.querySelector('link[title="applied_subreddit_stylesheet"]').remove();
    for (let item of document.getElementsByClassName('addComment')) {
        item.addEventListener('click', function (e) {
            //Send the selection to the server to process.
            var thingId = e.target.id.substr(8);
            console.log("Adding comment ", thingId);
            e.target.style.display = 'none';
            document.getElementById('rvm_rem_' + thingId).style.display = 'inline';
            selection.push(thingId);
        });
    }
    for (let item of document.getElementsByClassName('removeComment')) {
        item.addEventListener('click', function (e) {
            //Send the selection to the server to process.
            var thingId = e.target.id.substr(8);
            console.log("Removing comment ", thingId);
            e.target.style.display = 'none';
            document.getElementById('rvm_rem_' + thingId).style.display = 'inline';
            for (var i = 0; i < selection.length; i++) {
                if (selection[i] === thingId) {
                    selection.splice(i, 1);
                    e.target.style.display = 'none';
                    document.getElementById('rvm_add_' + thingId).style.display = 'inline';
                    return;
                }
            }
        });
    }
    document.getElementById('sendBtn').addEventListener('click', function () {
        //Send the selection to the server to process.

        //Get if this is the first/last post in the video.
        var postType = document.querySelector("input[name='postType']:checked").value;
        //Get if this is supposed to be featured
        var isFeatured = document.getElementById("isFeatured").checked;
        //Get the thumbnail text for this post.
        var thumbnailText = document.getElementsByName("thumbnailText")[0].value;

        //Stringify the selection, separated by commas.
        var stringifiedSelection = "";
        selection.forEach(function (e) {
            stringifiedSelection += e + ",";
        });
        stringifiedSelection = stringifiedSelection.substring(0, stringifiedSelection.length - 1);
        //Combine it unto a query string & navigate to it.
        window.location.href = '?selection=' + encodeURIComponent(stringifiedSelection) +
                '&capture=true&postType=' + postType + '&isFeatured=' + isFeatured + '&thumbnailText=' +
                encodeURIComponent(thumbnailText);
    });
};