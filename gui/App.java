package gui;
//Tenkaichi ANM Tail Replacer by ViveTheModder
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import cmd.Main;

public class App 
{
	public static int anmCnt;
	public static JProgressBar bar;
	private static File lastFolder;
	private static File[] currFolders = new File[2];
	private static final Font BOLD = new Font("Tahoma", 1, 24);
	private static final Font BOLD_S = new Font("Tahoma", 1, 14);
	private static final Font MED = new Font("Tahoma", 0, 18);
	private static final String HTML_A_START = "<html><a href=''>";
	private static final String HTML_A_END = "</a></html>";
	private static final String WINDOW_TITLE = "Tenkaichi ANM Bone Replacer v1.3";
	private static final Toolkit DEF_TOOLKIT = Toolkit.getDefaultToolkit();

	public static void errorBeep()
	{
		Runnable runWinErrorSnd = (Runnable) DEF_TOOLKIT.getDesktopProperty("win.sound.exclamation");
		if (runWinErrorSnd!=null) runWinErrorSnd.run();
	}
	private static File getFolderFromFileChooser()
	{
		File folder=null;
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setDialogTitle("Select Folder with ANM Files...");
		if (lastFolder!=null) chooser.setCurrentDirectory(lastFolder);
		while (folder==null)
		{
			int result = chooser.showOpenDialog(chooser);
			if (result==0)
			{
				folder = chooser.getSelectedFile();
				lastFolder=folder;
			}
			else break;
		}
		return folder;
	}
	private static void setApp(String[] boneNames)
	{
		String[] text = {"Source","Destination","Start","End","first","last"};
		//initialize components
		GridBagConstraints gbc = new GridBagConstraints();
		JButton apply = new JButton("Replace Bone Contents");
		JComboBox<String>[] dropdowns = new JComboBox[2];
		JFrame frame = new JFrame(WINDOW_TITLE);
		JLabel[] dropdownLabels = new JLabel[2];
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		JMenu helpMenu = new JMenu("Help");
		JMenuItem about = new JMenuItem("About");
		JMenuItem[] folderSelects = new JMenuItem[2];
		JPanel panel = new JPanel(new GridBagLayout());
		//set component properties
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		for (int i=0; i<2; i++) 
		{
			folderSelects[i] = new JMenuItem("Select "+text[i]+" ANM Folder...");
			fileMenu.add(folderSelects[i]);
			dropdownLabels[i] = new JLabel("Bone Selection "+text[2+i]);
			dropdowns[i] = new JComboBox<String>(boneNames);
			dropdownLabels[i].setToolTipText("The "+text[4+i]+" bone included in the selection.");
			dropdownLabels[i].setFont(BOLD);
			dropdowns[i].setFont(MED);
			//thank goodness for the default renderer
			((JLabel)dropdowns[i].getRenderer()).setHorizontalAlignment(JLabel.CENTER);
		}
		apply.setFont(BOLD_S);
		apply.setToolTipText("Empty ANMs will be skipped.");
		//add action listeners
		about.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				Box horBox = Box.createHorizontalBox();
				JLabel label = new JLabel("Made by: ");
				JLabel author = new JLabel(HTML_A_START+"ViveTheModder"+HTML_A_END);
				author.addMouseListener(new MouseAdapter() 
				{
					@Override
					public void mouseClicked(MouseEvent e) 
					{
						try 
						{
							Desktop.getDesktop().browse(new URI("https://github.com/ViveTheModder"));
						} 
						catch (IOException | URISyntaxException e1) 
						{
							errorBeep();
							JOptionPane.showMessageDialog(frame, e1.getClass().getSimpleName()+": "+e1.getMessage(), "Exception", 0);
						}
					}});
				horBox.add(label);
				horBox.add(author);
				JOptionPane.showMessageDialog(null, horBox, WINDOW_TITLE, 1);
			}
		});
		apply.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				String errors="";
				for (int i=0; i<2; i++)
				{
					if (currFolders[i]==null) errors+="No "+text[i]+" ANM folder has been selected!\n";
				}
				if (!errors.equals(""))
				{
					errorBeep();
					JOptionPane.showMessageDialog(null, errors, WINDOW_TITLE, 0);
				}
				else 
				{
					int[] boneIds = new int[2];
					for (int i=0; i<2; i++) boneIds[i] = dropdowns[i].getSelectedIndex();
					setProgress(frame,boneIds);
				}
			}
		});
		for (int i=0; i<2; i++)
		{
			final int index=i;
			folderSelects[i].addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					currFolders[index] = getFolderFromFileChooser();
				}
			});
		}
		//add components
		helpMenu.add(about);
		menuBar.add(fileMenu);
		menuBar.add(helpMenu);
		for (int i=0; i<2; i++)
		{
			panel.add(dropdownLabels[i],gbc);
			panel.add(dropdowns[i],gbc);
			panel.add(new JLabel(" "),gbc);
		}
		panel.add(apply,gbc);
		frame.add(panel);
		//set frame properties
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setJMenuBar(menuBar);
		frame.setSize(512,256);
		frame.setTitle(WINDOW_TITLE);
		frame.setVisible(true);
	}
	private static void setProgress(JFrame frame, int[] boneIds)
	{
		//change progress bar settings (must be done before declaring)
	    UIManager.put("ProgressBar.background", Color.WHITE);
	    UIManager.put("ProgressBar.foreground", Color.GREEN);
	    UIManager.put("ProgressBar.selectionBackground", Color.BLACK);
	    UIManager.put("ProgressBar.selectionForeground", Color.BLACK);
	    //initialize components
	    Dimension barSize = new Dimension(256,32);
	    GridBagConstraints gbc = new GridBagConstraints();
	    JDialog dialog = new JDialog();
	    JPanel panel = new JPanel(new GridBagLayout());
		JLabel label = new JLabel("Replacing bone contents...");
		bar = new JProgressBar();
		//set component properties
		bar.setValue(0);
		bar.setStringPainted(true);
		bar.setBorderPainted(true);
		bar.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.LIGHT_GRAY));
		bar.setFont(BOLD_S);
		bar.setMinimumSize(barSize);
		bar.setMaximumSize(barSize);
		bar.setPreferredSize(barSize);
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		label.setFont(BOLD);
		//add components
		panel.add(label,gbc);
		panel.add(new JLabel(" "),gbc);
		panel.add(bar,gbc);
	    dialog.add(panel);
	    dialog.setTitle(WINDOW_TITLE);
	    dialog.setSize(512,256);
	    dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	    dialog.setLocationRelativeTo(null);
	    dialog.setVisible(true);
	    
	    SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>()
		{
			@Override
			protected Void doInBackground() throws Exception 
			{
				long start = System.currentTimeMillis();
				frame.setEnabled(false);
				String error = Main.writeSrcAnms(currFolders, boneIds, null);
				long finish = System.currentTimeMillis();
				double time = (finish-start)/1000.0;
				
				if (!error.equals(""))
				{
					dialog.dispose();
					errorBeep();
					JOptionPane.showMessageDialog(null, error, App.WINDOW_TITLE, 0);
				}
				else
				{
					DEF_TOOLKIT.beep();
					JOptionPane.showMessageDialog(null, anmCnt+" ANM files have been changed in "+time+" seconds!", App.WINDOW_TITLE, 1);
					dialog.dispose();
				}
				frame.setEnabled(true);
				return null;
			}
		};
		worker.execute();
	}
	public static void main(String[] args) 
	{
		try 
		{
			String[] boneNames = Main.getBoneNames(new File("bone-ids.csv"));
			String[] boneNamesSimple = new String[55];
			System.arraycopy(boneNames, 0, boneNamesSimple, 0, boneNamesSimple.length);
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			setApp(boneNamesSimple);
		} 
		catch (Exception e) 
		{
			errorBeep();
			JOptionPane.showMessageDialog(null, e.getClass().getSimpleName()+": "+e.getMessage(), "Exception", 0);		
		}
	}
}