const functions = require('firebase-functions');

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//  response.send("Hello from Firebase!");
// });

//let functions = require('firebase-functions');

const admin = require('firebase-admin');

admin.initializeApp(functions.config().firebase);

exports.sendNotification = functions.database.ref('/Messages/{userId}/{messageId}')
    .onCreate((dataSnapshot,context) => {

     const recipientId = context.params.userId;
     console.log("recipientId: ", recipientId);

     const messageId = context.params.messageId;
     console.log("messageId: ",messageId);

     const senderId = dataSnapshot.val().sender_id;
     console.log("senderId: ", senderId);

     console.log(dataSnapshot);

//	//get the userId of the person receiving the notification because we need to get their token
////	const receiverId = event.params.userId;
//    const receiverId = event.child('recipient_id').val();
//	console.log("receiverId: ", receiverId);
//
//	//get the user id of the person who sent the message
//	const senderId = event.child('sender_id').val();
//	console.log("senderId: ", senderId);
//
//	//get the message
//	const message = event.child('message').val();
//	console.log("message: ", message);
//
//	//get the message id. We'll be sending this in the payload
//	const messageId = event.params.messageId;
//	console.log("messageId: ", messageId);
//
	//query the Users node and get the name of the user who sent the message
	return admin.database().ref("/Users/" + senderId).once('value').then(snap => {
		const senderFName = snap.child("fname").val();
		const senderLName = snap.child("lname").val();
		console.log("senderName: ", senderFName," ",senderLName);

		//query the Messages node and get the message of the user who will receive the message
		return admin.database().ref("/Messages/" + recipientId + "/" + messageId).once('value').then(snap => {
        		const message = snap.child("message").val();
        		console.log("message: ", message);

                    //get the token of the user receiving the message
                    return admin.database().ref("/Users/" + recipientId).once('value').then(snap => {
                    	const token = snap.child("msg_token").val();
                    	console.log("token: ", token);

                    	//we have everything we need
                    	//Build the message payload and send the message
                    	console.log("Constructing the notification message.");
                    	const payload = {
                    	    notification: {
                                title: "New Message from " + senderFName + " " + senderLName,
                                body: message,
                                sound: "default",
                                priority: "max"
                            },
                    		data: {
                    		    data_type: "direct_message",
                    			title: "New Message from " + senderFName + " " + senderLName,
                    			sound: "default",
                    			message: message,
                    			message_id: messageId
                    		}
                    	};

                    	return admin.messaging().sendToDevice(token, payload)
                    		.then(function(response) {
                    			console.log("Successfully sent message:", response);
                    		})
                    		.catch(function(error) {
                    			console.log("Error sending message:", error);
                    		});
                    	});

        });

	});
});
