var nodemailer = require('nodemailer');

function sendVerificationEmail(email, code){
    var transporter = nodemailer.createTransport({
         service: 'gmail',
         auth: {
             user: 'TinSlam.V3Bash@gmail.com',
             pass: 'w32E*zoA!39*94@a'
         }
    });

    var mailOptions = {
         from: 'TinSlam.V3Bash@gmail.com',
         to: email,
         subject: 'تایید پست الکترونیکی',
         html: '<h1 dir="rtl">به اینجا خوش آمدید !</h1><p dir="rtl">رمز تایید شما عبارت است از : <br>' + code + '<br></p>'
    };

    transporter.sendMail(mailOptions, function(error, info){
         if (error) {
             console.log(error);
         } else {
             console.log('Email sent: ' + info.response);
         }
    });
}

module.exports.sendVerificationEmail = sendVerificationEmail;