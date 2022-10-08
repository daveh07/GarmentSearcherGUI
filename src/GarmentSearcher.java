import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;

public class GarmentSearcher {

    //container fields
    private static JFrame mainWindow=null; //main container
    private static JPanel searchView = null; //view 1
    private static JPanel userInformationView = null; //view
    private static CriteriaEntry criteriaEntry; //object used for inputting user info/filters

    //app fields
    private static ImageIcon icon = null;
    private static final String filePath = "./inventory.txt";
    private static String iconFilePath = "./icon.txt";
    private static final String appName = "Garment Geek";
    private static Inventory allGarments = null;


    public static void main(String[] args) {
        allGarments = loadInventory(filePath);
        mainWindow = new JFrame(appName);
        mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        icon = new ImageIcon(iconFilePath);
        mainWindow.setIconImage(icon.getImage());
        mainWindow.setMinimumSize(new Dimension(650,700));
        searchView = enterCriteria();
        mainWindow.setContentPane(searchView);
        mainWindow.pack();
        mainWindow.setVisible(true);
    }

    /**
     * this method creates a panel to be used as the only container in the main frame while users enter search criteria
     * It instantiates the src.CriteriaEntry class to get user input panel and input values
     * @return a JPanel representing the search view (data entry and submit button)
     */
    public static JPanel enterCriteria(){
        JPanel searchWindow = new JPanel();
        searchWindow.setLayout(new BorderLayout());

        criteriaEntry = new CriteriaEntry(allGarments.getTypeToBrandMapping(), allGarments.highestPrice());
        searchWindow.add(criteriaEntry.generateSearchView(),BorderLayout.CENTER);

        JButton submitInfo = new JButton("Submit");
        ActionListener actionListener = e -> conductSearch(criteriaEntry);
        submitInfo.addActionListener(actionListener);
        searchWindow.add(submitInfo,BorderLayout.SOUTH);

        return searchWindow;
    }


    public static void conductSearch(CriteriaEntry criteriaEntry){
        //this map will store the user's selections
        Map<Filter,Object> specs = new HashMap<>();
        GarmentType garmentType = criteriaEntry.getSelectedGarmentType();
        String brand = criteriaEntry.getSelectedBrand();
        double minPrice = criteriaEntry.getMinPrice();
        double maxPrice = criteriaEntry.getMaxPrice();
        Material material = criteriaEntry.getMaterial();

        //Error handling for as user must select garment type.
        if(garmentType.equals(GarmentType.SELECT_TYPE)){
            JOptionPane.showMessageDialog(mainWindow,"You MUST select a garment type.\n","Invalid search",JOptionPane.INFORMATION_MESSAGE,icon);
            return;
        }
        //add the user's choices to the map
        specs.put(Filter.GARMENT_TYPE,garmentType);
        if(brand!=null) specs.put(Filter.BRAND,brand);
        if(!material.equals(Material.NA))specs.put(Filter.MATERIAL,material); //only add it if the user has selected material type
        GarmentSpecs garmentSpecs = new GarmentSpecs(specs,minPrice,maxPrice);
        List<Garment> compatibleGarments = allGarments.findMatch(garmentSpecs);

        showResults(compatibleGarments);
    }

    public static void showResults(List<Garment> compatibleGarments){
        //if there are compatible garments, create a HashMap to link the user's selections to Garment objects
        if(compatibleGarments.size()>0) {
            Map<String, Garment> options = new LinkedHashMap<>();
            options.put("Select garment",null);
            //create a panel to contain the descriptions of the compatible garments
            JPanel garmentDescriptions = new JPanel();
            garmentDescriptions.setBorder(BorderFactory.createTitledBorder("Matches found!! The following garments meet your criteria: "));
            garmentDescriptions.setLayout(new BoxLayout(garmentDescriptions,BoxLayout.Y_AXIS));
            garmentDescriptions.add(Box.createRigidArea(new Dimension(0,10)));

            //create a new, non-editable text area for each garment
            for (Garment compatibleGarment : compatibleGarments) {
                garmentDescriptions.add(describeIndividualGarment(compatibleGarment));
                options.put(compatibleGarment.getName() + " (" + compatibleGarment.getGarmentSpecs() + ")", compatibleGarment);
            }
            JScrollPane verticalScrollBar = new JScrollPane(garmentDescriptions);
            verticalScrollBar.setPreferredSize(new Dimension(300, 450));
            verticalScrollBar.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            SwingUtilities.invokeLater(() -> verticalScrollBar.getViewport().setViewPosition( new Point(0, 0) ));

            JComboBox<String> optionsCombo = new JComboBox<>(options.keySet().toArray(new String[0]));

            ActionListener actionListener = e -> checkUserGarmentSelection(options,optionsCombo);
            optionsCombo.addActionListener(actionListener);

            //give the user the option to return to view 1 (to search again)
            JButton goBackToSearch = new JButton("Back to search");
            goBackToSearch.addActionListener(e -> {
                mainWindow.setContentPane(searchView);
                mainWindow.revalidate();
            });

            //alternatively, give users the option to submit an order
            JButton sendMessage = new JButton("Send message");

            //this panel is for the dropdown list - it contains a border, with instructional title, the dropdown list and rigid areas for padding
            JPanel selectionPanel = new JPanel();
            selectionPanel.setLayout(new BoxLayout(selectionPanel,BoxLayout.LINE_AXIS));
            selectionPanel.add(Box.createRigidArea(new Dimension(0,20)));
            selectionPanel.add(Box.createRigidArea(new Dimension(10,0)));
            selectionPanel.add(optionsCombo);
            selectionPanel.add(Box.createRigidArea(new Dimension(10,0)));
            selectionPanel.add(goBackToSearch);
            selectionPanel.add(Box.createRigidArea(new Dimension(10,0)));
            selectionPanel.add(sendMessage);
            selectionPanel.add(Box.createRigidArea(new Dimension(10,0)));
            selectionPanel.add(Box.createRigidArea(new Dimension(0,20)));

            //this is the overall panel (view 2) used to display the compatible garments and the dropdown list
            JPanel results = new JPanel();
            results.setLayout(new BorderLayout());
            results.add(Box.createRigidArea(new Dimension(0,10)),BorderLayout.NORTH);
            results.add(verticalScrollBar,BorderLayout.CENTER);
            results.add(selectionPanel,BorderLayout.SOUTH);

            //set main window to the results panel (view 2)
            mainWindow.setContentPane(results);
            mainWindow.revalidate();
        }
        else{
            //if there are no compatible garments, let the user know using a popup window
            JOptionPane.showMessageDialog(searchView,"Unfortunately none of our garments meet your criteria.\n","No Matching Criteria",JOptionPane.INFORMATION_MESSAGE,icon);
        }
    }

    /**
     * method to describe an individual garment, within a non-editable JTextArea
     * @param garment garment to describe
     * @return a JTextArea
     */
    public static JTextArea describeIndividualGarment(Garment garment){
        JTextArea garmentDescription = new JTextArea(garment.getGarmentInformation(Filter.values()));
        garmentDescription.setEditable(false);
        //this will ensure that if the description is long, it 'overflows'
        garmentDescription.setLineWrap(true);
        garmentDescription.setWrapStyleWord(true);
        return garmentDescription;
    }

    /**
     * this method checks whether the user has selected 'none' or an actual garment
     * it calls appropriate methods based on the user's selection.
     * @param options a Map that links garment names to garment objects
     */
    public static void checkUserGarmentSelection(Map<String, Garment> options, JComboBox<String> optionsCombo){

        String garmentName = (String) optionsCombo.getSelectedItem();

        if(options.get(garmentName)!=null) {
            Garment chosenGarment = options.get(garmentName);
            placeOrderRequest(chosenGarment);
        }
    }

    //Parts 4 and 6.3 and 7
    /**
     * @param lineToWrite the String to be written to the file
     * a method to write a user's message or order request to a file
     */
    public static void writeMessageToFile(String lineToWrite){
        String filePath = criteriaEntry.getName().replace(" ","_")+"_query.txt";
        Path path = Path.of(filePath);
        try {
            Files.writeString(path, lineToWrite);
            JOptionPane.showMessageDialog(mainWindow,"Thank you for your message. \nOne of our friendly staff will be in touch shortly. \nClose this dialog to terminate."
                    ,"Message Sent", JOptionPane.INFORMATION_MESSAGE);
            System.exit(0);
        }catch (IOException io){
            JOptionPane.showMessageDialog(mainWindow,"Error: Message could not be sent! Please try again!"
                    ,null, JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * this method allows the user to place an order request by entering their details into a contact form
     * to place an order request. It writes the garment and person's details
     * to a file once the user clicks "submit"
     */
    public static void placeOrderRequest(Garment chosenGarment){
        //instruct the user to fill out the form
        JLabel garmentMessage = new JLabel("To place an order request for "+chosenGarment.getName()+" fill in the form below");
        garmentMessage.setAlignmentX(0);
        JScrollPane jScrollPane = new JScrollPane(describeIndividualGarment(chosenGarment));
        jScrollPane.getViewport().setPreferredSize(new Dimension(300,150));
        jScrollPane.setAlignmentX(0);

        //add both the instruction and garment description to a new panel
        JPanel garmentDescriptionPanel = new JPanel();
        garmentDescriptionPanel.setAlignmentX(0);
        garmentDescriptionPanel.add(garmentMessage);
        garmentDescriptionPanel.add(jScrollPane);

        //use the contactForm method to get a panel containing components that allow the user to input info
        JPanel userInputPanel = criteriaEntry.contactForm();
        userInputPanel.setAlignmentX(0);
        //create a button, which when clicked, writes the user's request to a file
        JButton submit = new JButton("Submit");
        ActionListener actionListener = e -> {
            String lineToWrite = "Name: "+criteriaEntry.getName()+" \nEmail: "+criteriaEntry.getEmail()+"\nPhone number: "
                    +criteriaEntry.getPhoneNumber()+"\n\nMessage: "+criteriaEntry.getMessage()+
                    "\n\n"+criteriaEntry.getName()+" wishes to adopt "+chosenGarment.getName()+" ("+chosenGarment.getDescription()+")";
            writeMessageToFile(lineToWrite);
        };
        submit.addActionListener(actionListener);

        //add the garment description panel, contact form panel and button to a new frame, and assign it to view 3
        JPanel mainFramePanel = new JPanel();
        mainFramePanel.setLayout(new BorderLayout());
        mainFramePanel.add(garmentDescriptionPanel,BorderLayout.NORTH);
        mainFramePanel.add(userInputPanel,BorderLayout.CENTER);
        mainFramePanel.add(Box.createRigidArea(new Dimension(20,0)),BorderLayout.WEST);
        mainFramePanel.add(Box.createRigidArea(new Dimension(20,0)),BorderLayout.EAST);
        mainFramePanel.add(submit,BorderLayout.SOUTH);

        userInformationView = mainFramePanel;
        mainWindow.setContentPane(userInformationView);
        mainWindow.revalidate();
    }

    public static Geek getUserContactInfo(){
        String name = JOptionPane.showInputDialog(null,"Please enter your full name.",appName, JOptionPane.QUESTION_MESSAGE);
        if(name==null) System.exit(0);
        long phoneNumber=0;
        while(phoneNumber==0) {
            try {
                String userInput = JOptionPane.showInputDialog(null, "Please enter your phone number.", appName, JOptionPane.QUESTION_MESSAGE);
                if(userInput==null) System.exit(0);
                phoneNumber = Long.parseLong(userInput);
            } catch (NumberFormatException e) {
                phoneNumber = Long.parseLong(JOptionPane.showInputDialog(null, "Invalid entry. Please enter your phone number.", appName, JOptionPane.ERROR_MESSAGE));
            }
        }
        return new Geek(name,phoneNumber);
    }

    public static Inventory loadInventory(String filePath) {
        Inventory allGarments = new Inventory();
        Path path = Path.of(filePath);
        Map<Size,Integer> numOfGarmentSizes = new HashMap<>();
        List<String> fileContents = null;
        try {
            fileContents = Files.readAllLines(path);
        }catch (IOException io){
            System.out.println("File could not be found");
            System.exit(0);
        }
        for(int i=1;i<fileContents.size();i++){
            String[] info = fileContents.get(i).split("\\[");
            String[] singularInfo = info[0].split(",");
            String sizesRaw = info[1].replace("]","");
            String description = info[2].replace("]","");

            GarmentType garmentType = null;
            try {
                garmentType = GarmentType.valueOf(singularInfo[0].replace("-","_").toUpperCase()); //error catching
            }catch (IllegalArgumentException e){
                System.out.println("Error in file. type data could not be parsed for garment on line "+(i+1)+". Terminating. \nError message: "+e.getMessage());
                System.exit(0);
            }
            String name = singularInfo[1];

            long productCode = 0;
            try{
                productCode = Long.parseLong(singularInfo[2]);
            }catch (NumberFormatException n) {
                System.out.println("Error in file. Product code could not be parsed for garment on line "+(i+1)+". Terminating. \nError message: "+n.getMessage());
                System.exit(0);
            }

            double price = 0;
            try{
                price = Double.parseDouble(singularInfo[3]);
            }catch (NumberFormatException n){
                System.out.println("Error in file. Price could not be parsed for garment on line "+(i+1)+". Terminating. \nError message: "+n.getMessage());
                System.exit(0);
            }

            String brand = singularInfo[4];

            Material material = null;
            try{
                material = Material.valueOf(singularInfo[5].toUpperCase().replace(" ","_"));
            }catch (IllegalArgumentException e){
                System.out.println("Error in file. Material data could not be parsed for garment on line "+(i+1)+". Terminating. \nError message: "+e.getMessage());
                System.exit(0);
            }
            Neckline neckline = null;
            try{
                neckline = Neckline.valueOf(singularInfo[6].toUpperCase());
            }catch (IllegalArgumentException e){
                System.out.println("Error in file. Neckline data could not be parsed for t-shirt on line "+(i+1)+". Terminating. \nError message: "+e.getMessage());
                System.exit(0);
            }
            SleeveType sleeveType = null;
            try{
                sleeveType = SleeveType.valueOf(singularInfo[7].toUpperCase().replace(" ","_"));
            }catch (IllegalArgumentException e){
                System.out.println("Error in file. Sleeve type data could not be parsed for t-shirt on line "+(i+1)+". Terminating. \nError message: "+e.getMessage());
                System.exit(0);
            }
            PocketType pocketType = null;
            try{
                pocketType = PocketType.valueOf(singularInfo[8].toUpperCase());
            }catch (IllegalArgumentException e){
                System.out.println("Error in file. Pocket type data could not be parsed for hoodie on line "+(i+1)+". Terminating. \nError message: "+e.getMessage());
                System.exit(0);
            }
            HoodieStyle hoodieStyle = null;
            try{
                hoodieStyle = HoodieStyle.valueOf(singularInfo[9].toUpperCase().replace(" ","_"));
            }catch (IllegalArgumentException e){
                System.out.println("Error in file. Style data could not be parsed for hoodie on line "+(i+1)+". Terminating. \nError message: "+e.getMessage());
                System.exit(0);
            }

            Set<Size> sizes = new HashSet<>();
            for(String s: sizesRaw.split(",")){
                Size size = Size.S;
                try {
                    size = Size.valueOf(s);
//                    if(!numOfGarmentSizes.containsKey(size)) numOfGarmentSizes.put(size,1);
//                    else numOfGarmentSizes.put(size,numOfGarmentSizes.get(size)+1);
                }catch (IllegalArgumentException e){
                    System.out.println("Error in file. Size data could not be parsed for t-shirt on line "+(i+1)+". Terminating. \nError message: "+e.getMessage());
                    System.exit(0);
                }
                sizes.add(size);
            }

            Map<Filter,Object> filterMap = new LinkedHashMap<>();
            filterMap.put(Filter.GARMENT_TYPE,garmentType);
            filterMap.put(Filter.BRAND,brand);
            filterMap.put(Filter.MATERIAL,material);
            filterMap.put(Filter.SIZE,sizes);
            if(!neckline.equals(Neckline.NA)) filterMap.put(Filter.NECKLINE,neckline);
            if(!sleeveType.equals(SleeveType.NA)) filterMap.put(Filter.SLEEVE_TYPE,sleeveType);
            if(!hoodieStyle.equals(HoodieStyle.NA)) filterMap.put(Filter.HOODIE_STYLE,hoodieStyle);
            if(!pocketType.equals(PocketType.NA)) filterMap.put(Filter.POCKET_TYPE,pocketType);

            GarmentSpecs dreamGarment = new GarmentSpecs(filterMap);

            Garment Garment = new Garment(name,productCode,price,description,dreamGarment);
            allGarments.addGarment(Garment);
        }
        return allGarments;
    }
}
