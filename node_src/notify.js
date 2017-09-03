'use strict';

const notifier = require('node-notifier');
const path = require('path');

var title = process.argv[2];
var message = process.argv[3];
var image = process.argv[4];

notifier.notify({
    'title': title,
    'message': message,
    'icon': void 0,
    'contentImage':path.join(__dirname, 'images/' + image),
    'sound': true
});
