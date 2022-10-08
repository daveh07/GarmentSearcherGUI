import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;

public class CriteriaEntry {

    //fields
    private JComboBox<GarmentType> typeSelection;
    private GarmentType selectedGarmentType;
    private JComboBox<String> brandSelection;
    private JComboBox<String> sizeSelection;
    private String selectedBrand;
    private String selectedSize;
    private Material material;
    private Size sizes;

    private static JLabel feedback;
    private final JLabel feedbackMin = new JLabel(" "); //set to blank to start with
    private final JLabel feedbackMax = new JLabel(" ");
    private double minPrice=0;
    private double maxPrice=0;

    private JTextField name;
    private JTextField email;
    private JTextField phoneNumber;
    private JTextArea message;

    private final Map<GarmentType,Set<String>> garmentTypeToBrand;
    private static Map<Size, Integer> numGarmentSizes;
    private final double highestPrice;

    private BufferedImage image;

    /**
     * @param garmentTypeToBrand a mapping of garment type, e.g. garment to brand, e.g. Lacoder, Tommy Bugfinder, etc.
     * @param highestPrice the highest price in the database
     */
    public CriteriaEntry(Map<GarmentType,Set<String>> garmentTypeToBrand, double highestPrice){
        this.garmentTypeToBrand = new HashMap<>(garmentTypeToBrand);
        this.highestPrice = highestPrice;
    }

    /**
     * generates the search view for garment type and brand, price range and size
     * @return the described JPanel
     */
    public JPanel generateSearchView(){
        JPanel criteria = new JPanel();
        //set the layout to BoxLayout - Y axis, so that all the components are vertically stacked
        criteria.setLayout(new BoxLayout(criteria,BoxLayout.Y_AXIS));
        criteria.add(getUserInputGarmentType());
        criteria.add(getUserInputGarmentBrand());
        criteria.add(getUserInputGarmentSize());
        criteria.add(getUserInputPriceRange());
        criteria.add(getUserInputMaterial());
        criteria.add(getImages());

        return criteria;
    }

    /**
     * generates a JPanel containing dropdown lists for garment type and brand
     * @return a JPanel that allows users to select both garment type and brand
     */
    public JPanel getUserInputGarmentType(){
        //this populates the dropdown list with all the garment types
        typeSelection = new JComboBox<>(GarmentType.values());
        typeSelection.setAlignmentX(0);
        typeSelection.setPreferredSize(new Dimension(150,30)); //sizes the dropdown list
        typeSelection.requestFocusInWindow();
        //this prevents the dropdown list from automatically selecting a type of garment
        typeSelection.setSelectedItem(GarmentType.SELECT_TYPE);
        selectedGarmentType = GarmentType.SELECT_TYPE; //initialise the user's type selection to the dummy value
        //if the user chooses a garment type, call ifTypeSelected - this will populate the brands list based on the garment type selection
        typeSelection.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) ifTypeSelected();
        });

        JPanel brandTypeSelectionPanel = new JPanel();
        brandTypeSelectionPanel.setAlignmentX(0);
        brandTypeSelectionPanel.setBorder(BorderFactory.createTitledBorder("Garment Type:"));
        //Panel formatting
        brandTypeSelectionPanel.add(Box.createRigidArea(new Dimension(0,50))); //padding on top of the lists
        brandTypeSelectionPanel.add(Box.createRigidArea(new Dimension(10,0))); //padding on the left
        brandTypeSelectionPanel.add(typeSelection);
        brandTypeSelectionPanel.setPreferredSize(new Dimension(650, 80));
        brandTypeSelectionPanel.setMaximumSize(brandTypeSelectionPanel.getPreferredSize());
        brandTypeSelectionPanel.setMinimumSize(brandTypeSelectionPanel.getPreferredSize());
        return brandTypeSelectionPanel;
    }

    /**
     * this method handles the situation where the user selects a garment type
     * it populates the brands dropdown list with values based on the type
     * it disables the brands list if no type has been selected
     */
    private void ifTypeSelected(){
        //set the field selectedGarmentType to the user's choice
        selectedGarmentType=(GarmentType) typeSelection.getSelectedItem();

        //if the user hasn't selected the dummy value, get all the relevant brands (from the map), assigning them to the relevant brands field
        assert selectedGarmentType != null;
        Set<String> relevantBrands;

        //only populate the brands dropdown list if the type has been selected
        if(!selectedGarmentType.equals(GarmentType.SELECT_TYPE)) {
            relevantBrands = garmentTypeToBrand.get(selectedGarmentType);

            brandSelection.setEnabled(true); //enable it to allow the user to choose a brand
        }

        //if the user has selected the dummy value, set the relevant brands field to an empty set
        else {
            brandSelection.setEnabled(false); //disable it
            relevantBrands = Collections.emptySet();
        }

        //once the relevant brands field has been initialised, populate the brands drop down list
        brandSelection.setModel(new DefaultComboBoxModel<>(relevantBrands.toArray(new String[0])));

        //set the default item in the brands drop down list to the first element
        selectedBrand = null; //as the brand is optional, let's initialise the user's choice to null

        //request the program direct the user to the brands
        brandSelection.requestFocusInWindow();
    }


    /**
     * generates a JPanel containing dropdown lists for garment type and brand
     * @return a JPanel that allows users to select both type and breed (interdependent)
     */
    public JPanel getUserInputGarmentBrand(){

        //request that the user select their preferred brand
        brandSelection = new JComboBox<>(); //you could replace this with a JList for multi-breed selection
        brandSelection.addItem("Select Brand");
        brandSelection.setAlignmentX(0);
        brandSelection.setPreferredSize(new Dimension(150,30));

        if(selectedGarmentType.equals(GarmentType.SELECT_TYPE)) brandSelection.setEnabled(false); //disable it for as long as the user hasn't selected a garment type
        brandSelection.addItemListener(f -> {
            if (f.getStateChange() == ItemEvent.SELECTED) selectedBrand= (String) brandSelection.getSelectedItem();
        });

        //create a panel for the 2 dropdown lists
        JPanel brandSelectionPanel = new JPanel();
        brandSelectionPanel.setAlignmentX(0);
        brandSelectionPanel.setBorder(BorderFactory.createTitledBorder("Select Brand"));
        brandSelectionPanel.add(Box.createRigidArea(new Dimension(30,0)));
        brandSelectionPanel.add(brandSelection);
        brandSelectionPanel.add(Box.createRigidArea(new Dimension(10,0)));
        brandSelectionPanel.setPreferredSize(new Dimension(650, 80));
        brandSelectionPanel.setMaximumSize(brandSelectionPanel.getPreferredSize());
        brandSelectionPanel.setMinimumSize(brandSelectionPanel.getPreferredSize());

        return brandSelectionPanel;
    }

    /**
     * a method to get the user to select a breed from a dropdown list
     * the method displays the results of calling the checkBreed method
     * @return a JPanel containing a JList with the garment sizes for the user to select
     */
    public JPanel getUserInputGarmentSize(){
        feedback = new JLabel(" ");

        JList<Size> userSizeSelect = new JList<>(Size.values());
//        JList<Size> userSizeSelect = new JList<>(numGarmentSizes.keySet().toArray(new Size[0]));
        userSizeSelect.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

//        ListSelectionListener listener = e -> checkSize(userSizeSelect, numGarmentSizes);
//        userSizeSelect.addListSelectionListener(listener);

        JScrollPane vert_scrollPanel = new JScrollPane(userSizeSelect);
        vert_scrollPanel.setPreferredSize(new Dimension(500, 100));

        JPanel sizeSelectionPanel = new JPanel(); //defaults to flow layout
        sizeSelectionPanel.setAlignmentX(0); //ensure that you left align it
        sizeSelectionPanel.setBorder(BorderFactory.createTitledBorder("Select Size"));
        sizeSelectionPanel.add(vert_scrollPanel , BorderLayout.CENTER);
        sizeSelectionPanel.setPreferredSize(new Dimension(650, 150));
        sizeSelectionPanel.setMaximumSize(sizeSelectionPanel.getPreferredSize());
        sizeSelectionPanel.setMinimumSize(sizeSelectionPanel.getPreferredSize());
        sizeSelectionPanel.add(feedback);

        return sizeSelectionPanel;
    }

    /**
     * a method used by an action listener to output a String message as feedback
     * informing the user of how many garments are their chosen brand
     * @param numGarmentSizes a mapping of brands to number of garments of each brand
     *
     *                        This line of code has been commented out as I cannot get it working the way
     *                        I intend it to work
     *
     */
//    public void checkSize(JList<Size> userSizeSelection, Map<Size, Integer> numGarmentSizes){
//        List<Size> chosenSizes = userSizeSelection.getSelectedValuesList();
//        Set<Size> availableSizes = new HashSet<>(chosenSizes);
//        //Part 4.4: In the checkBreed method, use retainAll to get the
//        //overlap between the user's selections, and the available breeds.
//
//        availableSizes.retainAll(numGarmentSizes.keySet());
//
//        StringBuilder tempFeedback = new StringBuilder("Great news! We have");
//        if(availableSizes.size()==0){
//            feedback.setForeground(Color.RED);
//            feedback.setText("Unfortunately, we don't have any of your selected sizes available to order!");
//            userSizeSelection.requestFocusInWindow();
//        }
//        else{
//            feedback.setForeground(Color.BLUE);
//            //Part 4.5: Iterate over all these breeds, creating a String that informs
//            //the user how many of each of their desired breeds are available.
//            for(Size size: availableSizes){
//                int numOfSizes = numGarmentSizes.get(size);
//                if(numOfSizes==0) continue;
//                tempFeedback.append(" ").append(numOfSizes);
//                if(numOfSizes==1) tempFeedback.append(" ").append(size).append(",");
//                else tempFeedback.append(" ").append(size).append("s,");
//            }
//            tempFeedback.append(" available to order!");
//            feedback.setText(String.valueOf(tempFeedback));
//        }
//    }


    /**
     * method used to get and validate user input for price range
     * @return a JPanel containing instructions, text fields for price input and feedback for validation
     */
    public JPanel getUserInputPriceRange(){

        JLabel minLabel = new JLabel("Min. price");
        JLabel maxLabel = new JLabel("Max. price");

        //create text boxes
        JTextField min = new JTextField(4);
        JTextField max = new JTextField(4);

        //set default values for the price range text boxes (editable)
        min.setText(String.valueOf(0));
        max.setText(String.valueOf(highestPrice));
        maxPrice=highestPrice;

        //set the font and color of the feedback messages
        feedbackMin.setFont(new Font("", Font. ITALIC, 12));
        feedbackMin.setForeground(Color.RED);
        feedbackMax.setFont(new Font("", Font. ITALIC, 12));
        feedbackMax.setForeground(Color.RED);

        //add the document listeners
        min.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                //if the check min method returns false, request user addresses invalid input
                if(!checkMin(min)) min.requestFocus();
                checkMax(max); //after min has been updated, check max is still valid
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                //removing and inserting should be subjected to the same checks
                if(!checkMin(min))min.requestFocus();
                checkMax(max);
            }
            @Override
            public void changedUpdate(DocumentEvent e) {}
        });
        max.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if(!checkMax(max)) max.requestFocusInWindow();
                checkMin(min);
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                if(!checkMax(max))max.requestFocusInWindow();
                checkMin(min);
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });

        //add the text fields and labels to a panel
        JPanel priceRangePanel = new JPanel();
        priceRangePanel.add(minLabel);
        priceRangePanel.add(min);
        priceRangePanel.add(maxLabel);
        priceRangePanel.add(max);

        JPanel pricePanel = new JPanel();
        pricePanel.setBorder(BorderFactory.createTitledBorder("Price range"));
        pricePanel.setLayout(new BoxLayout(pricePanel,BoxLayout.Y_AXIS));
        pricePanel.setAlignmentX(0);
        pricePanel.add(priceRangePanel);
        feedbackMin.setAlignmentX(0);
        feedbackMax.setAlignmentX(0);
        pricePanel.add(feedbackMin);
        pricePanel.add(feedbackMax);
        pricePanel.setPreferredSize(new Dimension(650, 80));
        pricePanel.setMaximumSize(pricePanel.getPreferredSize());
        pricePanel.setMinimumSize(pricePanel.getPreferredSize());

        return pricePanel;
    }

    /**
     * validates user input for min price
     * @param minEntry the JTextField used to enter min price
     * @return true if valid price, false if invalid
     */
    private boolean checkMin(JTextField minEntry){
        feedbackMin.setText("");
        try{
            int tempMin = Integer.parseInt(minEntry.getText());
            if(tempMin < 0 || tempMin>maxPrice) {
                feedbackMin.setText("Price must be >= "+0+" and <= "+maxPrice+". Defaulting to "+minPrice+" - "+maxPrice+".");
                return false;
            }else {
                minPrice=tempMin;
                feedbackMin.setText("");
                return true;
            }
        }catch (NumberFormatException n){
            feedbackMin.setText("Please enter a valid number for min price. Defaulting to "+minPrice+" - "+minPrice+".");
            minEntry.selectAll();
            return false;
        }
    }

    /**
     * validates user input for max price
     * @param maxEntry the JTextField used to enter max price
     * @return true if valid price, false if invalid
     */
    private boolean checkMax(JTextField maxEntry){
        feedbackMax.setText("");
        try{
            int tempMax = Integer.parseInt(maxEntry.getText());
            if(tempMax < minPrice) {
                feedbackMax.setText("Max. price must be >= min. price. Defaulting to "+minPrice+" - "+maxPrice+".");
                maxEntry.selectAll();
                return false;
            }else {
                maxPrice = tempMax;
                feedbackMax.setText("");
                return true;
            }
        }catch (NumberFormatException n){
            feedbackMax.setText("Please enter a valid number for max. price range. Defaulting to "+minPrice+" - "+maxPrice+".");
            maxEntry.selectAll();
            return false;
        }
    }

    /**
     * method used to get user selection of material - Cotton, Wool or Polyester
     * Use of radio buttons
     * @return a JPanel containing the relevant radio buttons and labels
     */
    public JPanel getUserInputMaterial(){
        ButtonGroup materialButtonGroup = new ButtonGroup();
        JRadioButton cotton = new JRadioButton(Material.COTTON.toString());
        JRadioButton polyester = new JRadioButton(Material.POLYESTER.toString());
        JRadioButton wool = new JRadioButton(Material.WOOL_BLEND.toString());
        materialButtonGroup.add(cotton);
        materialButtonGroup.add(polyester);
        materialButtonGroup.add(wool);
        cotton.setActionCommand(Material.COTTON.toString());
        cotton.setActionCommand(Material.POLYESTER.toString());
        cotton.setActionCommand(Material.WOOL_BLEND.toString());

        JPanel materialPanel = new JPanel();
        materialPanel.setAlignmentX(0);
        materialPanel.setBorder(BorderFactory.createTitledBorder("Which material would you prefer your garment to be? (Optional)"));
        materialPanel.add(cotton);
        materialPanel.add(polyester);
        materialPanel.add(wool);
        materialPanel.setPreferredSize(new Dimension(650, 80));
        materialPanel.setMaximumSize(materialPanel.getPreferredSize());
        materialPanel.setMinimumSize(materialPanel.getPreferredSize());

        material  = Material.NA;
        ActionListener actionListener = e-> material = Material.valueOf(materialButtonGroup.getSelection().getActionCommand().toUpperCase());
        cotton.addActionListener(actionListener);
        polyester.addActionListener(actionListener);
        wool.addActionListener(actionListener);

        return materialPanel;
    }

    /**
     * method used to display images of garments
     * @return a JPanel containing the images
     */
    public JPanel getImages() {

        BufferedImage image1 = null;
        BufferedImage image2 = null;
        BufferedImage image3 = null;

        try {
            image1 = ImageIO.read(new File("./breakingBad_.jpg"));
            image2 = ImageIO.read(new File("./keepCalm_.jpg"));
            image3 = ImageIO.read(new File("./walkingDead_.png"));
        } catch (IOException ex) {
            System.out.println("Image path could not be found");
        }

        assert image1 != null;
        assert image2 != null;
        assert image3 != null;

        JLabel picLabel1 = new JLabel(new ImageIcon(image1));
        JLabel picLabel2 = new JLabel(new ImageIcon(image2));
        JLabel picLabel3 = new JLabel(new ImageIcon(image3));

        //create a panel for the 2 dropdown lists
        JPanel imageSelectionPanel = new JPanel();
        imageSelectionPanel.setAlignmentX(0);
        imageSelectionPanel.setBorder(BorderFactory.createTitledBorder("Example Garments"));

        imageSelectionPanel.add(Box.createRigidArea(new Dimension(30, 0)));
        imageSelectionPanel.add(picLabel1);
        imageSelectionPanel.add(picLabel2);
        imageSelectionPanel.add(picLabel3);
        imageSelectionPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        imageSelectionPanel.setPreferredSize(new Dimension(650, 200));
        imageSelectionPanel.setMaximumSize(imageSelectionPanel.getPreferredSize());
        imageSelectionPanel.setMinimumSize(imageSelectionPanel.getPreferredSize());

        return imageSelectionPanel;
    }

    public static Map<String, Integer> loadGarments(String filePath){
        Map<String,Integer> numOfEachBrand = new HashMap<>();
        List<String> eachBrand = new ArrayList<>();
        try{
            Path path = Path.of(filePath);
            eachBrand = Files.readAllLines(path);
        }catch (IOException e){
            JOptionPane.showMessageDialog(null,"Could not load file! Username cannot be verified!");
            System.exit(0);
        }
        for (int i=1;i<eachBrand.size();i++) {
            String[] elements = eachBrand.get(i).split(",");
            String garmentBrand = elements[4].replaceAll("\n", "");

            if(garmentBrand.equalsIgnoreCase("t-shirt")){
                String brand = elements[4].toLowerCase();
                if(!numOfEachBrand.containsKey(brand)) numOfEachBrand.put(brand,1);
                else numOfEachBrand.put(brand,numOfEachBrand.get(brand)+1);
            }
        }
        return numOfEachBrand;
    }


    /**
     * a method to generate a JPanel containing a name, email, ph num and message fields
     * It can be used if user has selected 'none' or if they are ordering a garment
     * @return a JPanel as described
     */
    public JPanel contactForm(){
        //create labels and text fields for users to enter contact info and message
        JLabel enterName = new JLabel("Full name");
        name = new JTextField(12);
        JLabel enterEmail = new JLabel("Email address");
        email = new JTextField(12);
        JLabel enterPhoneNumber = new JLabel("Phone number");
        phoneNumber = new JTextField(12);
        JLabel enterMessage = new JLabel("Type your query below");
        message = new JTextArea(6,12);

        JScrollPane jScrollPane = new JScrollPane(message);
        jScrollPane.getViewport().setPreferredSize(new Dimension(250,100));

        //create a new panel, add padding and user entry boxes/messages to the panel
        JPanel userInputPanel = new JPanel();
        userInputPanel.setLayout(new BoxLayout(userInputPanel,BoxLayout.Y_AXIS));
        userInputPanel.add(Box.createRigidArea(new Dimension(0,10)));
        userInputPanel.setAlignmentX(0);
        enterName.setAlignmentX(0);
        name.setAlignmentX(0);
        userInputPanel.add(enterName);
        userInputPanel.add(name);
        userInputPanel.add(Box.createRigidArea(new Dimension(0,10)));
        enterEmail.setAlignmentX(0);
        email.setAlignmentX(0);
        userInputPanel.add(enterEmail);
        userInputPanel.add(email);
        userInputPanel.add(Box.createRigidArea(new Dimension(0,10)));
        enterPhoneNumber.setAlignmentX(0);
        phoneNumber.setAlignmentX(0);
        userInputPanel.add(enterPhoneNumber);
        userInputPanel.add(phoneNumber);
        userInputPanel.add(Box.createRigidArea(new Dimension(0,10)));
        enterMessage.setAlignmentX(0);
        message.setAlignmentX(0);
        userInputPanel.add(enterMessage);
        jScrollPane.setAlignmentX(0);
        userInputPanel.add(jScrollPane);
        userInputPanel.add(Box.createRigidArea(new Dimension(0,10)));

        return userInputPanel;
    }


    /**
     * Getters - used to access values the user has entered when an object of this class is created
     * @return users min price for garment search
     */
    public double getMinPrice() {
        return minPrice;
    }

    /**
     * @return users max price for garment search
     */
    public double getMaxPrice() {
        return maxPrice;
    }

    /**
     * @return Get the selected garment type object selected by the user
     */
    public GarmentType getSelectedGarmentType(){
        return selectedGarmentType;
    }

    /**
     * @return Get the selected garment brand selected by the user
     */
    public String getSelectedBrand(){
        return selectedBrand;
    }

    /**
     * @return Get the selected garment material selected by the user
     */
    public Material getMaterial(){
        return material;
    }

    /**
     * @return Get the selected garment name selected by the user
     */
    public String getName() {
        return name.getText();
    }

    /**
     * @return Get the users email address
     */
    public String getEmail() {
        return email.getText();
    }

    /**
     * @return  Get the users phone number
     */
    public String getPhoneNumber() {
        return phoneNumber.getText();
    }

    /**
     * @return  Get the users message
     */
    public String getMessage() {
        return message.getText();
    }
}



























