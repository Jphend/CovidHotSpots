const mongoose = require('mongoose')
require('mongoose-schema-jsonschema')(mongoose);

const locationSchema = new mongoose.Schema({
    email: {
        type: String,
        required: true
    },
     // type: {
     //     type: String,
     //     enum: ['Point'],
     //     required: true
     // },
      // coordinates: {
      //     type: [],
      //    required: true
      // }
    // coordinates:
    //      {
    //          type: [],
    //          lat: {
    //             type: [],
    //             required: true
    //          },
    //          lng: {
    //             type: [],
    //             required: true
    //          }
    //     }
    lat: {
        type: [Number],
        required: true
    },
    lng: {
        type: [Number],
        required: true
    },
});

//const jsonSchema = locationSchema.jsonSchema();
//console.dir(jsonSchema, { depth: null });

module.exports = locationSchema