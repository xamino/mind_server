function send(type, data) {
    $.ajax({
        url: '/Servlet/test',
        type: type,
        data: JSON.stringify(data),
        contentType: 'application/json; charset=utf-8',
        dataType: 'json',
        async: false
    });
}