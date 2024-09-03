# ONLYOFFICE DocSpace app for Pipedrive

This app allows working with office files related to your [Pipedrive Deals](https://www.pipedrive.com/) in ONLYOFFICE DocSpace rooms.

## App installation and configuration 

ONLYOFFICE DocSpace app can be installed via the Pipedrive Marketplace. **Please note:** each user needs to install the DocSpace app themselves. The Pipedrive admin is not able to install the app for everyone at once.

The Pipedrive admin can configure the app via the **Marketplace apps** section within Pipedrive: Tools and apps -> Marketplace apps -> ONLYOFFICE DocSpace app.

### Connection settings (for Pipedrive administrators)

At first, go to your DocSpace -> Developer Tools -> JavaScript SDK. Add the addresses of your Pipedrive and ONLYOFFICE DocSpace in the section **Enter the address of DocSpace to embed**. 

Once done, go the ONLYOFFICE DocSpace app settings within Pipedrive, fill in the **DocSpace Service Address** field and click the Connect button.

If the connection is successful, two buttons will appear on this page:

- **Change**: ability to connect another DocSpace. The data in the current DocSpace will not be deleted.
- **Disconnect**: completely disables the app (clearing user authorization and hooks). In this case, the connection of the Pipedrive Users group with Pipedrive will be removed. We recommended using this option only if there is a need to completely clear data in the DocSpace app.

### Authorization settings (for Pipedrive administrators)

This section is available once the **Connection settings** are configured. 

Here, enter the DocSpace login and password. 

For users with the Deal Admin role, the **System User** checkbox is shown. By default, if no user who has installed the DocSpace app is a System User, this checkbox will be enabled and cannot be disabled. Until a System User is set, other DocSpace users cannot log in. 

If you need the System User to be linked to another account, another user with the Deal Admin rights can enable the System User checkbox when logging in.

### Authorization settings (for regular Pipedrive users)

Once the previous steps have been completed by the Pipedrive administrator, regular Pipedrive users can log into DocSpace with their credentials.

## App usage

The ONLYOFFICE DocSpace app for Pipedrive can be accessed via the Deals section of Pipedrive: go to the corresponding deal and find the DocSpace frame.

For each deal, you can create a separate DocSpace room in which the deal participants can work together on office documents. To create a room, click the **Create room** button.

The room is named according to the rule *Pipedrive - Company name - Deal name*. Inside the DocSpace frame, deal participants can work depending on their access rights.

### Access rights

All Pipedrive users who logged into the DocSpace app are added to the **Pipedrive Users (Company Name)** group. If a user logs out of DocSpace, they will be removed from the group.

Access rights to the room within a deal are determined by the Pipedrive access rights to the corresponding deal.

- **All users**: the room becomes available to the Pipedrive Users group as well as the followers are invited by name (only those who have installed the DocSpace app and have been authorized).
- **Item owner**: the room is accessible only to the deal owner and the followers by name. A mandatory condition in this case is that the deal owner and the followers must install the DocSpace app and pass authorization.
- **Item owner’s visibility group**: only the followers are synchronized (available in the paid version of Pipedrive). 
- **Item owner’s visibility group and sub-group**: only the followers are synchronized (available in the paid version of Pipedrive). 

## Important to know

If a Pipedrive user does not have access to the room within a deal, they can request access by clicking on the corresponding button. This action checks whether the user should have access to the room. If they should, the user is added to the room based on the Access Rights. If they should not, the corresponding notification is shown.
## Project info

Official website: [www.onlyoffice.com](https://www.onlyoffice.com/)

Code repository: [github.com/ONLYOFFICE/onlyoffice-docspace-pipedrive](https://github.com/ONLYOFFICE/onlyoffice-docspace-pipedrive)

## User feedback and support

In case of technical problems, the best way to get help is to submit your issues [here](https://github.com/ONLYOFFICE/onlyoffice-docspace-pipedrive/issues). 
Alternatively, you can contact ONLYOFFICE team on [forum.onlyoffice.com](https://forum.onlyoffice.com/).