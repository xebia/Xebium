
(function (doc) {
    if (!doc.querySelectorAll) return;

    var toc = doc.querySelectorAll('.nav a');
    var sections = _.map(toc, function (e) { return doc.querySelector(e.getAttribute('href')); });

    function update_page () {
        var current_page = window.location.hash || '#features';
        _.each(sections, function (e) { e.style.display = ('#' + e.id === current_page) ? 'block' : 'none'; });
    }

    _.each(toc, function (e) { e.onclick = function () { setTimeout(update_page, 5); } });

    update_page();
        
})(window.document);
