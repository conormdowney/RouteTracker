I wanted to make a short read me file to explain the app.

--------OVERVIEW----------
The app itself can be used to track a users route. So if they go on a run somewhere, they can track their run. Then they can load up files and effectively race against the file (i.e. themselves or a friend). 

I am using the Open Street Map SDK for Android. I chose to use this as opposed to Google maps as I wanted to se what it would be like to work with a third party sdk in an android app.

--------FEATURES----------
Below is an outline of some of the features of the app:

As mentioned above it is possible to either create a route of your own or to race a loaded route.

The GPS Location updates are handled in a service and so the phone does not need to be left on in order for a route to be tracked.

A check is done to ensure the GPS is turned on before starting.

The menu in the toolbar holds a number of options: 

Screenshot - for allowing a screenshot to be taken that will be uploaded to social media.
Route List - which is a list of all save files.
Weather Check - which will go and check the weather for the week for either the current location or the location of a loaded route.

The Route List opens up a new activity that will show a list of all the files a user can choose. 
Each file is an item in a list view that shows the start address, the end address and the distance of the route.
A user can delete files directly from here by a long hold on an item.
If they select an item it loads up the initial map activity and displays the whole route.
A user can then choose to race that route. The app will colour the route with the same time its creator ran it. In this way a user can race their friend just with the app.
This activity uses a custom Array Adapter and an Async Task for getting the addresses from the route gps co-ordinates

Weather Check opens up a list of the next 7 days that shows the weather in the current location or a routes location. 
It will show the high and low temperatures and the day and general weather forecast.
This also uses a custom Array Adapter and an AsyncTask for getting the weather using the Open Weather Map API.
If a user clicks on a day it opens up the calendar app and they can make a reminder.

When a user is finished they can choose to save a route or not.


--------NOTE----------
Please note that the app is not 100% completed. I have much more I want to get into it. I do not have the social media integration included yet. Neither do I have a way to transfer files. I am looking at using Bluetooth. There are some icons missing, mostly because I have not found ones yet. 

This is something I will be continuously adding to and updating. I hope that what it shows already will prove I can program Android, and you can see it is just a matter of me learning what I do not already know.