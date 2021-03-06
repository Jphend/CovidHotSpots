const mongoose = require('mongoose')
require('mongoose-schema-jsonschema')(mongoose);

const locationSchema = new mongoose.Schema({
    email: {
        type: String,
        required: true
    },
    lat: {
        type: [Number],
        required: true
    },
    lng: {
        type: [Number],
        required: true
    },
});

module.exports = locationSchema