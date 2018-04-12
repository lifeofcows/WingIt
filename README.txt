COMP4601, Assignment 2
By Avery Vine (100999500) and Maxim Kuzmenko (101002578)

In Eclipse, import the COMP4601-Assignment2 project as an Existing project in the Workspace, and then click Finish. For that project, go to the build path and correct the JRE if unbound, and then go to Project Facets and select “Dynamic Web Project” and “Java”, and apply and close.
Also, add the project as a Web Module to the Tomcat server, and edit the Path to link to COMP4601-RS. 

Then click run. To initialize the database with values, go to
http://localhost:8080/COMP4601-RS/rest/rs/reset/{dir} where dir is the file path to the collection of users/genres that need to be crawled.

Other paths and description of their uses are specified in the ANALYSIS.pdf document linked. 