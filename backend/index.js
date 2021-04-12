const url = "mongodb+srv://jonwil:Hpkjw%4019@Cluster1.zyqmu.mongodb.net/covidhotspots?retryWrites=true&w=majority";
const express = require('express');
const app = express();
const bodyParser = require('body-parser');
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true}))
const crypto = require('crypto');
const mongoose = require('mongoose');
const userSchema = require('./userSchema.js');
const locationSchema = require('./locationSchema.js');
const User = mongoose.model('users', userSchema, 'users');
const Location = mongoose.model('locations', locationSchema, 'locations');

async function createUser(email, name, password, salt) {
    return new User({
        email,
        name,
        password,
        salt
    }).save()
}

async function createLocationEntry(email, lat, lng) {
    return new Location({
        email,
        type: 'Point',
        lat: [lat],
        lng: [lng]
    }).save()
}

async function addNewLocation(email, lat, lng) {
    //await Location.findOneAndUpdate({'email':email}, {$push: {coordinates: {$each: [latLng]}}}, {useFindAndModify:false})
    await Location.findOneAndUpdate({'email':email}, {$push: {lat: {$each: [lat]}}}, {useFindAndModify:false})
    await Location.findOneAndUpdate({'email':email}, {$push: {lng: {$each: [lng]}}}, {useFindAndModify:false})

}

// async function getLocations(email) {
//     app.get('/', async(req, result) => {
//         await Location.findOne({'email': email}, {lat: 1, lng: 1, _id: 0}).lean().then(res => {
//             console.log(JSON.stringify(res));
//             result.send(JSON.stringify(res));
//         }).catch(err => console.error(`Fatal error occurred: ${err}`));
//     })
// }

const genRandomString = function (length) {
    return crypto.randomBytes(Math.ceil(length / 2)).toString('hex').slice(0, length)
};

const sha512 = function(password, salt) {
    const hash = crypto.createHmac('sha512', salt);
    hash.update(password);
    const value = hash.digest('hex');
    return {
        salt:salt,
        passwordHash:value
    };
};

function saltHashPassword(userPassword) {
    const salt = genRandomString(16);
    return sha512(userPassword, salt);
}

function checkHashPassword(userPassword, salt) {
    return sha512(userPassword, salt);
}

function getRandomInRange(from, to, fixed) {
    return (Math.random() * (to - from) + from).toFixed(fixed) * 1;
    // .toFixed() returns string, so ' * 1' is a trick to convert to number
}

;(async () => {
    await mongoose.connect(url, {useNewUrlParser: true, useUnifiedTopology: true})

    app.post('/register', (request, response) => {
        const postData = request.body;
        const plainTextPassword = postData.password;
        const hash_data = saltHashPassword(plainTextPassword);
        const password = hash_data.passwordHash;
        const salt = hash_data.salt;
        const name = postData.name;
        const email = postData.email;

        User.find({'email': email}).countDocuments(function (err, number) {
            console.log(number);
            if (number > 0) {
                response.json('Email already exists');
                console.log('Email already exists');
            } else {
                    //User.create({email: email, password: plainTextPassword}, function (err, doc) { console.log(err); });
                    createUser(email, name, password, salt);
                    response.json('Registration successful');
                    console.log('Registration successful');
                }
        })

    });

    app.post('/', async(req, result) => {
        const postData = req.body;
        const email = postData.email;

        Location.find({'email': email}, {lat: 1, lng: 1, _id: 0}).lean().then(res => {
            console.log(JSON.stringify(res));
            result.send(JSON.stringify(res));
        }).catch(err => console.error(`Fatal error occurred: ${err}`));
    })

    app.post('/simulate', async(req, result) => {
        const postData = req.body;
        const lat = postData.lat;
        const lng = postData.lng;
        var i = 0;
        const coordinates = [];
        while(i<50000) {
            const latitude = getRandomInRange(-90, 90, 5);
            const longitude = getRandomInRange(-180, 180, 5);

            const latLng = new google.maps.LatLng(latitude, longitude);
            coordinates.push(latLng);
            i++;
        }
        console.log(coordinates);
        result.send(coordinates);

    })

    app.get('/showAll', (request, response) => {
        Location.aggregate(
            [
                {$unwind: '$lat'},
                {$unwind: '$lng'},
                {$group: {_id:0, lat: {$addToSet: '$lat'}, lng: {$addToSet: '$lng'}}}
            ]
        ).then(res => {console.log(res); response.send(res);})
    });

    app.post('/login', (request, response) => {
        const postData = request.body;
        const email = postData.email;
        const userPassword = postData.password;

        User.findOne({'email': email}).countDocuments(function (err, number) {
            if (number === 0) {
                response.json('Email does not exist, please register');
                console.log('Email does not exist, please register');
            } else {
                User.findOne({'email': email}, function (err, user) {
                    const salt = user.salt;
                    const hashedPassword = checkHashPassword(userPassword, salt).passwordHash;
                    const encryptedPassword = user.password;
                    if (hashedPassword === encryptedPassword) {
                        response.json('Login Success!');
                        console.log('Login Success!')
                        //getLocations(email);
                    }
                    else {
                        response.json('Login Fail, email or password is incorrect!');
                        console.log('Login Fail!');
                    }
                })
            }
        })
    });

    app.post('/clickLocation', (request, response) => {
        const postData = request.body;
        const email = postData.email;
        const lat = postData.lat;
        const lng = postData.lng;

        Location.findOne({'email': email}).countDocuments(function (err, number) {
            if (number === 0) {
                response.json('No user location entries made yet');
                console.log('No user location entries made yet');
                createLocationEntry(email, lat, lng);

            } else {
                addNewLocation(email, lat, lng);
                response.json('New user location added');
                console.log('New user location added');
            }
        })
    });

    app.post('/searchLocation', (request, response) => {
        const postData = request.body;
        const email = postData.email;
        //const latLng = postData.coordinates;
        const lat = postData.lat;
        const lng = postData.lng;
        //createLocationEntry(email, point);

        Location.findOne({'email': email}).countDocuments(function (err, number) {
            if (number === 0) {
                response.json('No user location entries made yet');
                console.log('No user location entries made yet');
                createLocationEntry(email, lat, lng);
            } else {
                //console.log(email);
                //console.log(point);
                addNewLocation(email, lat, lng);
                response.json('New user location added');
                console.log('New user location added');
            }
        })
    });

    app.listen(3000, ()=>{
        console.log('Connected to mongodb!');
    })

    //process.exit(0)
})()
