package testWin;

import java.awt.BorderLayout;
import java.awt.Checkbox;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.filechooser.FileSystemView;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.myliqj.dir.DateUtils;
import com.myliqj.dir.GetDirInfo;
import com.myliqj.dir.StringHelper;
import com.myliqj.dir.bean.DirInfo;
import com.myliqj.dir.bean.FileInfo;

public class TestMain /*extends ApplicationWindow*/ implements MouseListener,KeyListener,ActionListener  {

//	private static final FormToolkit formToolkit = new FormToolkit(
//			Display.getDefault()); 
//	Display display = Display.getDefault();
//	private Shell shell = null;
	
	private JFrame frame;
	private JTextField textField;
	private JTextField outputText;
	private JButton btnNewButton;
	private JCheckBox isOutput;
	
	private JTree jtNetDevice;//���������
	private JScrollPane jspTree;//�����������
	private List<DirInfo> dirInfo;
	private DirInfo dir0;
	private JTabbedPane tabbedPane;
	private JPanel pnl_output_logs;
	private JPanel pnl_options;
	private JPanel pnl_filesize_top;
	private JTextArea textArea;
	private JTextArea textArea_log;
	private JTextField jtf_filesize_top;
	private Font curFont;
	private JPopupMenu popMenu;
	private JMenuItem copy,cut,paste,selectall;
	private JMenuItem open_tree,copy_name,copy_path,copy_tree;
	private JSeparator sep_tree,sep_text;
	private int curMousePos_x;
	private int curMousePos_y;
	private JTextField dt_format;
	private JCheckBox customDateTimeFormat;
	private Map options;
	private JTextField dateParseErrorCountMax;
	private JCheckBox skipSYMLINKD;
	
//	private static Shell myshell;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					TestMain window = new TestMain();
					window.frame.setVisible(true); 
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		 
//		TestMain wt=TestMain.getInstance();
//		wt.show(); 
	}
//	private static TestMain instance;
//	public static TestMain getInstance() {
//		if (instance == null)
//			instance = new TestMain();
//		return instance;
//	}
	
	/**
	 * Create the application.
	 */
	public TestMain() {
//		super(null);

		curFont = new Font("����",0,14);
		InitGlobalFont(curFont);
		initialize();
	}
	public void initMenu(){
		popMenu = new JPopupMenu();
		popMenu.add(copy = new JMenuItem("����"));
		popMenu.add(paste = new JMenuItem("ճ��"));
		popMenu.add(cut = new JMenuItem("����"));
		popMenu.add(sep_text = new JSeparator());
		popMenu.add(selectall = new JMenuItem("ȫѡ"));
		
		// tree:open_tree,copy_name,copy_path,copy_tree
		popMenu.add(sep_tree = new JSeparator());
		popMenu.add(open_tree = new JMenuItem("ˢ����Ŀ¼"));
		popMenu.add(copy_name = new JMenuItem("��������"));
		popMenu.add(copy_path = new JMenuItem("����·��"));
		popMenu.add(copy_tree = new JMenuItem("���Ʊ�Ŀ¼����Ŀ¼��1�㣩"));
		

		copy.addActionListener(this);
		paste.addActionListener(this);
		cut.addActionListener(this);
		selectall.addActionListener(this);
		open_tree.addActionListener(this);
		copy_name.addActionListener(this);
		copy_path.addActionListener(this);
		copy_tree.addActionListener(this);
	};
	
	@Override
	public void actionPerformed(ActionEvent e) {
		action(e);
	}
	public void action(ActionEvent e) {
		String str = e.getActionCommand();
		JTextComponent text = null; 
		if (e!=null && e.getSource() instanceof JMenuItem){
			JMenuItem mi = (JMenuItem)e.getSource() ;
			JPopupMenu pop = (JPopupMenu) ( mi.getParent() );
			if (pop!=null){
				Component invokerObj = pop.getInvoker();
				if (invokerObj instanceof JTextComponent){ 
					text = (JTextComponent)pop.getInvoker();
					if (str.equals(copy.getText())) { // ����
						text.copy();
					} else if (str.equals(paste.getText())) { // ճ��
						text.paste();
					} else if (str.equals(cut.getText())) { // ����
						text.cut();
					} else if (str.equals(selectall.getText())) { // ȫ��
						text.selectAll();
					}
				}else if (invokerObj instanceof JTree) {
					String mess = null;
					
					// ���¼��� x/y
//					JTree jt = (JTree) invokerObj;
//					Point pt = jt.getLocationOnScreen();
//					Point pt2 = pop.getLocation(); 
//					int x = pt2.x - pt.x;
//					int y = pt2.x - pt.y;
					int x = curMousePos_x; int y = curMousePos_y;
					
					if (str.equals(open_tree.getText())) { // �򿪽ڵ�
						this.open_tree(x, y);
					} else if (str.equals(copy_name.getText())) { // ���ƽڵ� ����
						mess = copy_tree(x, y,"copy_name");
					} else if (str.equals(copy_path.getText())) { // ���ƽڵ� ·��
						mess = copy_tree(x, y,"copy_path");
					} else if (str.equals(copy_tree.getText())) { // ���������ӽڵ� ·��
						mess = copy_tree(x, y,"copy_tree");
					}
					if (mess!=null && !"".equals(mess)){
						// ���Ƶ����а�
						copyStringToClipboard(mess);
					}
					
				}
			}
		}
	}

	/**
	 * ���а����Ƿ����ı����ݿɹ�ճ��
	 * 
	 * @return trueΪ���ı�����
	 */
	public boolean isClipboardString() {
		boolean b = false;
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		Transferable content = clipboard.getContents(this);
		try {
			if (content.getTransferData(DataFlavor.stringFlavor) instanceof String) {
				b = true;
			}
			
		} catch (Exception e) {
		}
		return b;
	}
	public void copyStringToClipboard(String str){
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents( new StringSelection(str) , null);
	}
	
	/**
	 * �ı�������Ƿ�߱����Ƶ�����
	 * 
	 * @return trueΪ�߱�
	 */
	public boolean isCanCopy(JTextComponent ta) {
		boolean b = false;
		int start = ta.getSelectionStart();
		int end = ta.getSelectionEnd();
		if (start != end)
			b = true;
		return b;
	}

	/**
	 * �ı�������Ƿ�߱�ѡ������
	 * 
	 * @return trueΪ�߱�
	 */
	public boolean isCanSelectAll(JTextComponent ta) {
		return !"".equals(ta.getText());
	}
	private void setPopStatu(JTextComponent text){
		boolean isVisible = text!=null; 
		copy.setVisible(isVisible);
		paste.setVisible(isVisible);
		cut.setVisible(isVisible);
		selectall.setVisible(isVisible);
		sep_text.setVisible(isVisible);
		if (!isVisible) return; 
		
		copy.setEnabled(text!=null && isCanCopy(text));
		paste.setEnabled(text!=null && isClipboardString());
		cut.setEnabled(text!=null && isCanCopy(text));
		selectall.setEnabled(text!=null && isCanSelectAll(text));
		sep_text.setEnabled(text!=null);
	}

	private void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3) {
					
					//showMessage(e.getX() + " " + e.getY());
					boolean isTree = false;
					if (e.getComponent() instanceof JTextComponent){
						setPopStatu((JTextComponent)e.getComponent());						
					}else if(e.getComponent() instanceof JTree){
						isTree = true;
						setPopStatu(null);
						paste.setEnabled(false);
						selectall.setEnabled(false);
					}
					sep_tree.setVisible(isTree);
					open_tree.setVisible(isTree);
					copy_name.setVisible(isTree);
					copy_path.setVisible(isTree);
					copy_tree.setVisible(isTree);
				}
				
				
				curMousePos_x = e.getX();
				curMousePos_y = e.getY();
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			private void showMenu(MouseEvent e) { 
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}	
	public void show(){
//		shell = new Shell();
//		initialize();
	}
	
	public void clearTree(){
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) jtNetDevice.getModel().getRoot();
		if (root != null){ 
			root.removeAllChildren();
		}
		
	}
	
	
	public void setOptions(String key,String value){
		if (options == null) options = new HashMap<String, String>();
		options.put(key, value);
	}
	public synchronized void fillTree(String path){
		
		clearTree();		
		if (path==null || "".equals(path)) return ;
		
		//List<DirInfo> dirInfo = null;
		this.dirInfo = null;
		try {
			btnNewButton.setEnabled(false);
			
			long topsize = 50L*1024*1024; // 50mb Ϊ����
			if(isOutput.isSelected()){
				try {
					double ts = Double.valueOf(jtf_filesize_top.getText().trim());
					if (ts>1000){
						ts = 1000;
						jtf_filesize_top.setText(""+ts);	
					}
					if (ts>0){
						topsize = Math.round(ts * 1024*1024);
					}
				} catch (Exception e) {
					jtf_filesize_top.setText("50");
					e.printStackTrace();
				}
			}
			Map map = null;
			
			PrintStream oldStream = System.out;
			try{ 
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				PrintStream cacheStream = new PrintStream(baos);
				System.setOut(cacheStream); 
				if (customDateTimeFormat.isSelected()){
					setOptions("setDateTimeFormat",dt_format.getText().trim());
				}
				setOptions("setDateParseErrorCountMax",dateParseErrorCountMax.getText().trim()); 
			    setOptions("setSkipSYMLINKD",skipSYMLINKD.isSelected()?"YES":"NO");
				
				map = GetDirInfo.getDirInfo(path,topsize,options);

				output_log("\n"+baos.toString());
				textArea_log.setCaretPosition(0);
			}finally{
				System.setOut(oldStream);
			}
			if (map==null) return;
			
			dirInfo = (List<DirInfo>)map.get("dir");
			
			List<FileInfo> fis = (List<FileInfo>) map.get("file_topsize");
			if (fis!=null){
				// ����ֵ �����򣬵���
				// ��ʾ
				textArea.setText("");
				StringBuilder sb = new StringBuilder();
				for (FileInfo fi : fis) {
					String len = StringHelper.readableFileSize(fi.getFile_size());
					sb.append((len.length()<10)?StringHelper.repeat(" ", 10-len.length()):"")
					  .append(len).append(" ").append(fi.getFile_name()).append("\n");
				}
				textArea.append(sb.toString());
				textArea.setCaretPosition(0);
//				textArea.insert(sb.toString(),0);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			return ;
		}finally{
			btnNewButton.setEnabled(true);
		}
		
		if (dirInfo == null || dirInfo.size()==0) {
			showMessage("ָ��Ŀ¼��"+path+" ���ļ�!");
			return;
		}
		
		dir0 = dirInfo.get(0); 
		
		String RootName = String.format("(%d) %s  %s %s  (%d)",dir0.getDir_sub_all_count(), dir0.getDir_name() 
				,StringHelper.readableFileSize(dir0.getDir_sub_file_size()), "100%", dir0.getDir_sub_file_count() );
		
		myObj myRoot= new myObj(dir0,RootName,false,-1);
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) jtNetDevice.getModel().getRoot();//new DefaultMutableTreeNode(dir0.getDir_name());
		//Object obj = root.getUserObject();
		//if (obj != myRoot){
			root.setUserObject(myRoot);
		//}
		loadLeafDir(root);
		 
	}
	
	public void loadLeafDir (DefaultMutableTreeNode node){
		if (node==null) return ;
		if (node.getChildCount()>0) return; 
        
        myObj curMyObj = (myObj) node.getUserObject();
        if (curMyObj.getIsLoad()) return;
        
        try{
        	btnNewButton.setEnabled(false);
	    	jtNetDevice.setEnabled(false);
	        long dirId = curMyObj.getDir().getDir_id();
	        int level = curMyObj.getDir().getLevel();
	        DefaultMutableTreeNode child = null;        
	        if (level>=0){        	
	        	int childCount = 0;
	        	for (DirInfo dir : dirInfo){
	        		
	        		int curlevel = dir.getLevel();
	        		boolean isAdd = (level==0 && curlevel==1) || (curlevel == level+1 && dirId == dir.getParent_dir_id()) ;
	        		
	    			if (isAdd){
	    				childCount ++;
	    				String dirName = dir.getDir_name(); 
	    				dirName = dirName.substring(0,dirName.length()-1);
	    				int pos = dirName.lastIndexOf('\\');
	    				if (pos>=0 && pos<dirName.length()){
	    					dirName = dirName.substring(pos+1,dirName.length());
	    				}
	    				
	    				String allSize = StringHelper.readableFileSize(dir.getDir_sub_file_size());
	    				String rate = String.format("%.2f", dir.getDir_sub_file_size().floatValue()/dir0.getDir_sub_file_size().floatValue()*100) +"%";
	
	    				child = new DefaultMutableTreeNode(new myObj(dir,String.format("(%d) %s  %s %s  (%d)",dir.getDir_sub_all_count(), dirName,allSize,rate, dir.getDir_sub_file_count() ),false,-1));
	    				node.add(child);
//	    				if (isOutput.isSelected()){
//	    					output_log(dir 
//		    						+ " , " + StringHelper.readableFileSize(dir.getDir_sub_file_size())
//		    						+ " , " + String.format("%.2f", dir.getDir_sub_file_size().floatValue()/dir0.getDir_sub_file_size().floatValue()*100) +"%");
//	    				}
	    			}
	    		}
	        	curMyObj.setIsLoad(true); // ������Σ����Ѽ��صı�ʶ������ȥ
	        	curMyObj.setChildCount(childCount);
	        	//curMyObj.setShow("(" + childCount + ") " + curMyObj.show);
	        	if (node.getChildCount()>0){
	        		//jtNetDevice.getModel().
	        		// �ѵ�һ���ӽڵ���ʾ����
	        		jtNetDevice.scrollPathToVisible( new TreePath( ( (DefaultMutableTreeNode)node.getChildAt(0) ).getPath() ) );
	        	}
	        	jtNetDevice.updateUI(); 
	        } 
	    }finally{
	    	btnNewButton.setEnabled(true);
	    	jtNetDevice.setEnabled(true);
	    }
	}

//	protected String folderDig(Shell parent,String setPath) {
//		// �½��ļ��У�Ŀ¼���Ի���
//		DirectoryDialog folderdlg = new DirectoryDialog(parent); //196608
//		
//		// �����ļ��Ի���ı���
//		folderdlg.setText("�ļ���ѡ��");
//		// ���ó�ʼ·��
//		if (setPath!=null){
//			folderdlg.setFilterPath(setPath);
//		}else{
//			folderdlg.setFilterPath("SystemDrive");			
//		}
//		// ���öԻ�����ʾ�ı���Ϣ
//		folderdlg.setMessage("��ѡ����Ӧ���ļ���");
//		// ���ļ��Ի��򣬷���ѡ���ļ���Ŀ¼
//		return folderdlg.open();
//	}
	class myObj{
		DirInfo dir; String show; boolean isLoad = false; int childCount = -1;
		public myObj(DirInfo dir,String show,boolean isLoad,int childCount){
			this.dir = dir;
			this.show = show;
			this.isLoad = isLoad;
			this.childCount = childCount;
		} 
		public DirInfo getDir(){
			return dir;
		}
		public boolean getIsLoad(){
			return isLoad;
		}
		public void setIsLoad(boolean isLoad){
			this.isLoad = isLoad;
		}

		public int getChildCount(){
			return childCount;
		}
		public void setChildCount(int childCount){
			this.childCount = childCount;
		}
		public String getShow(){
			return show;
		}
		public void setShow(String show){
			this.show = show;
		}
		@Override
		public String toString(){
			return show;
		}
	}

	/**
	 * ͳһ�������壬����������֮�������ɸ����������ӽ��涼����Ҫ�ٴ���������
	 */
	private static void InitGlobalFont(Font font) {
		FontUIResource fontRes = new FontUIResource(font);
		for (Enumeration<Object> keys = UIManager.getDefaults().keys(); keys
				.hasMoreElements();) {
			Object key = keys.nextElement();
			Object value = UIManager.get(key);
			if (value instanceof FontUIResource) {
				UIManager.put(key, fontRes);
			}
		}
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() { 
//		shell.setToolTipText("\u4E2D\u79D1\u4FDD(\u96C6\u7FA4)\u7CFB\u7EDF\u7EF4\u62A4\u5DE5\u5177");
//		shell.setSize(958, 645);
//		shell.setText("Ŀ¼�������� v1.0 by liqj 2018-04-26");
//		
//		Composite composite = new Composite(shell, SWT.NONE);
//		composite.setLayoutData(BorderLayout.NORTH);
//		composite.
		
		initMenu();
		
		frame = new JFrame();
		frame.setBounds(100, 100, 768, 465);
		frame.setTitle("WindowsĿ¼�ļ���С �������� v1.0 by liqj 2018");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout(5, 5));
//		frame.setVisible(true);
		
		
		JPanel panel = new JPanel();
		frame.getContentPane().add(panel, BorderLayout.NORTH);
		panel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
		
		btnNewButton = new JButton("����");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { 
				
//				String sPath = textField.getText().trim();
//				try{
//					btnNewButton.setText("���ڷ���...");
//					fillTree(sPath); 						
//				}finally{
//					btnNewButton.setText("����");
//				}
				
				new Thread() {
					public void run() {
						final String sPath = textField.getText().trim();
						try{
							btnNewButton.setText("���ڷ���...");

							jtNetDevice.setVisible(false); // ���ý���ˢ���ػ����������һ������ null ָ��
							fillTree(sPath); 						
						}finally{
							jtNetDevice.setVisible(true);
							btnNewButton.setText("����");
						}
					}
				}.start();
			}
		});
		
		
		panel.add(btnNewButton);
		
		textField = new JTextField();
		textField.setToolTipText("��ҪĿ¼���ļ�����·��Ҫ���ڣ��ɴ�*.ext����ʱֻ��֤ �ļ���С ҳ��������ļ�����ʾΪ dir �����(��Ҫָ����ʽ dir /a /-c /t:w /s)");
		textField.setText("D:\\java_run\\Quartz");
		panel.add(textField);
		//textField.setSize(1000, textField.getHeight());
		textField.setColumns(60);
		addPopup(textField,popMenu);
		/*
		 * ��ӻس�����
		 */
//		textField.addKeyListener(new KeyAdapter() {
//			public void keyReleased(KeyEvent e) { 
//				if (e.character == SWT.CR) {
//					String sPath = textField.getText().trim();
//					fillTree(sPath);  
//				}
//			}
//		});
		
		isOutput = new JCheckBox("��ȡ�ļ�Top");
		isOutput.setSelected(true);
		isOutput.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jtf_filesize_top.setEnabled(isOutput.isSelected());
			}
		});
		panel.add(isOutput);

		jtf_filesize_top = new JTextField("50");
		jtf_filesize_top.setColumns(6); //jf.setAlignmentX(0.5f); 
		jtf_filesize_top.setHorizontalAlignment(JTextField.CENTER);
		jtf_filesize_top.setEnabled(isOutput.isSelected());
		panel.add(jtf_filesize_top);
		panel.add(new JLabel("MB"));

		
		JButton button = new JButton("ѡ��·��");
		button.setVisible(false);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// 
				//��ȡѡ�нڵ�  
//                DefaultMutableTreeNode selectedNode  
//                    = (DefaultMutableTreeNode) jtNetDevice.getLastSelectedPathComponent();  
//                //����ڵ�Ϊ�գ�ֱ�ӷ���  
//                if (selectedNode == null) return;  
//                 
//                loadLeafDir(selectedNode);
				 
//				folderDig(Shell.win32_new((Display)null, 0));
				
				//String str = selectFilesAndDir().getAbsolutePath(); 
//				String str = folderDig(new Shell(SWT.MODELESS),textField.getText());// folderDig(new Shell(new Display(), SWT.APPLICATION_MODAL),textField.getText());
////				System.out.println(str);
//				if (str != null){
//					textField.setText(str);
//				}
                
			}
		});
		panel.add(button);
		
//		JTree tree = new JTree();
//		frame.getContentPane().add(tree, BorderLayout.CENTER);
//		

	    tabbedPane = new JTabbedPane(JTabbedPane.TOP);
	    frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);
	    
	    //panel_1 = new JPanel();
	    

		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("root");
		//rootNode.add(new DefaultMutableTreeNode("root2"));		
	    jtNetDevice = new JTree(rootNode);
	    jtNetDevice.setAutoscrolls(true);
	    jtNetDevice.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);//���õ�ѡģʽ
	    jspTree = new JScrollPane();
	    jspTree.setViewportView(jtNetDevice);

	    tabbedPane.addTab("Ŀ¼��С������", null, jspTree, "��Ŀ¼������ʽ�г�������Ŀ¼��˫�����Ҽ�����ˢ����Ŀ¼��������ռ�ÿռ�������");
	    //frame.getContentPane().add(jspTree, BorderLayout.CENTER);

		addPopup(jtNetDevice,popMenu);

	    pnl_filesize_top = new JPanel();
	    tabbedPane.addTab("�ļ���СTop���б�", null, pnl_filesize_top, "ָ�����õĴﵽ�򳬹�'��ȡ�ļ�Top'���ƣ��Ӵ���С���У�ÿ�η�������ϴν��");
	    pnl_filesize_top.setLayout(new BorderLayout(0, 0));
	    
	    textArea = new JTextArea();
	    textArea.setText("");
	    //textArea.setFont();
	    JScrollPane jsp_1 = new JScrollPane();
	    jsp_1.setViewportView(textArea);
	    pnl_filesize_top.add(jsp_1);
		addPopup(textArea,popMenu);
	    

	    pnl_output_logs = new JPanel();
	    tabbedPane.addTab("�����־", null, pnl_output_logs, "�����Ϣ��־��Ϣ����Ϣ�������У�������Ϣ�����ϣ�");
	    pnl_output_logs.setLayout(new BorderLayout(0, 0));
	    
	    textArea_log = new JTextArea();
	    textArea_log.setText("");
	    addPopup(textArea_log,popMenu);

	    JScrollPane jsp_2 = new JScrollPane();
	    jsp_2.setViewportView(textArea_log);
	    pnl_output_logs.add(jsp_2);
	    
	    
	    // ���� - ѡ��
	    pnl_options = new JPanel();
	    tabbedPane.addTab("����", null, pnl_options, "����-ѡ��");
	    //pnl_options.setLayout(new BorderLayout(0, 0));
		//panel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
	    pnl_options.setLayout(new FlowLayout(FlowLayout.LEFT));
	    //pnl_options.setBounds(41, 34, 313, 194);      
	    //pnl_options.setBorder(BorderFactory.createTitledBorder("����ʱ���ʽ:"));
	    
	    customDateTimeFormat = new JCheckBox("�Ƿ�ָ��Dir����������ʱ���ʽ�����Ͳ���ȷʱ��Ҫָ�� �� ��ϵͳ������Ϊ��׼��ʽ yyyy-MM-dd HH:mm��");
	    customDateTimeFormat.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {				
				dt_format.setEditable(customDateTimeFormat.isSelected());
			}
		});
	    pnl_options.add(customDateTimeFormat); 
	    dt_format = new JTextField();
	    dt_format.setColumns(30);
	    dt_format.setText(GetDirInfo.getSystemDateTimeFormat());
	    dt_format.setEditable(false);
	    pnl_options.add(dt_format);
	    
	    pnl_options.add(new JLabel("  �������ڸ�ʽ���������ֵ��"));
	    dateParseErrorCountMax = new JTextField("3000");
	    dateParseErrorCountMax.setColumns(10);
	    pnl_options.add(dateParseErrorCountMax);
	    
	    skipSYMLINKD = new JCheckBox("��������Ŀ¼ ���� SYMLINKD/JUNCTION ");
	    skipSYMLINKD.setSelected(true);
	    pnl_options.add(skipSYMLINKD);
	    
	    JPanel panel1 = new JPanel();
//	    panel1.setSize(panel1.getWidth(), 100);
	    panel1.setLayout(new GridLayout(2,1));

	    panel1.add(new JLabel(" ��ʽ��(��Ŀ¼����) ·�� ռ�ÿռ� �ٷֱ� (�ļ�����)"));
	    
	    outputText = new JTextField();
	    outputText.setText("");
//	    t1.setSize(t1.getWidth(), 100);
	    panel1.add(outputText);

	    addPopup(outputText,popMenu);
	    

//	    JTextField outputText2 = new JTextField();
//	    outputText2.setText("55");
//	    panel1.add(outputText2);
	    
	    frame.getContentPane().add(panel1,BorderLayout.SOUTH);
	    
	    jtNetDevice.addMouseListener(this);
		
	    
//	    shell.open();
//		shell.layout();
//		while (!shell.isDisposed()) {
//			if (!display.readAndDispatch()) {
//				display.sleep();
//			}
//		}
//		shell.dispose();
	}
	/**
     * ѡ��·�����ļ�
     * @return
     */
    public File selectFilesAndDir(){
        JFileChooser jfc=new JFileChooser();  
        //���õ�ǰ·��Ϊ����·��,�����ҵ��ĵ���ΪĬ��·��
        FileSystemView fsv = FileSystemView .getFileSystemView();
        jfc.setCurrentDirectory(fsv.getHomeDirectory());
        //JFileChooser.FILES_AND_DIRECTORIES ѡ��·�����ļ�
        jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES );  
        //��������ʾ��ı���
        jfc.showDialog(new JLabel(), "ȷ��");  
        //�û�ѡ���·�����ļ�
        File file=jfc.getSelectedFile();  
        return file;
    }
    
    public DefaultMutableTreeNode getCurrentTreeNode(int x,int y){
    	int selRow = jtNetDevice.getRowForLocation(x, y); // ��ǰ����Ӧλ��
        if (selRow<0) return null;
        TreePath selTree=jtNetDevice.getPathForRow(selRow);
        return (DefaultMutableTreeNode)selTree.getLastPathComponent();
    }
    public void open_tree(int x,int y){
        DefaultMutableTreeNode selNode=getCurrentTreeNode (x, y);
        if (selNode!=null && selNode.isLeaf()){ // ��Ҷ��
       		 loadLeafDir(selNode);
        }
    }
    public String copy_tree(int x,int y,String copy_lx){
        DefaultMutableTreeNode selNode=getCurrentTreeNode (x, y);
        if (selNode!=null){
        	Object obj = selNode.getUserObject();
 	        if (obj instanceof myObj){
 	        	if ("copy_name".equals(copy_lx)){
 	        		return ((myObj)obj).show; 
 	        	}else if("copy_path".equals(copy_lx)){
 	        		return ((myObj)obj).getDir().getDir_name();
 	        	}else if("copy_tree".equals(copy_lx)){
 	        		if (selNode.isLeaf()) loadLeafDir(selNode);
 	        		myObj myobj = ((myObj)obj);
	 	   	        long dirId = myobj.getDir().getDir_id();
	 	   	        int level = myobj.getDir().getLevel();
	 	   	        
 	        		StringBuilder sb = new StringBuilder();
 	        		//sb.append(myobj.getDir().getDir_name()).append("\n");
 	        		sb.append(myobj.show).append("\n");
 	        		for (DirInfo dir : dirInfo) {
						if ( (dir.getLevel() == level + 1) && (dirId == dir.getParent_dir_id())){
							// ���Ƹ�ʽΪ: cur-obj-name
							//             sub-obj-name
		    				String dirName = dir.getDir_name(); 
		    				dirName = dirName.substring(0,dirName.length()-1);
		    				int pos = dirName.lastIndexOf('\\');
		    				if (pos>=0 && pos<dirName.length()){
		    					dirName = dirName.substring(pos+1,dirName.length());
		    				}		    				
		    				String allSize = StringHelper.readableFileSize(dir.getDir_sub_file_size());
		    				String rate = String.format("%.2f", dir.getDir_sub_file_size().floatValue()/dir0.getDir_sub_file_size().floatValue()*100) +"%";
		    				sb.append(String.format("  (%d) %s  %s %s  (%d)\n",dir.getDir_sub_all_count(), 
		    						dirName,allSize,rate, dir.getDir_sub_file_count() ));
						}
					}
 	        		return sb.toString();
 	        	}
 	        } 
        }
        return "";
    }
    
    
	@Override
	public void mouseClicked(java.awt.event.MouseEvent e) {
		if (e.getClickCount() == 2 && e.getSource() == jtNetDevice) {
			open_tree(e.getX(), e.getY());
		} else if (e.getClickCount() == 1 && e.getSource() == jtNetDevice) {
			int selRow = jtNetDevice.getRowForLocation(e.getX(), e.getY());
			if (selRow < 0)
				return;
			TreePath selTree = jtNetDevice.getPathForRow(selRow);
			DefaultMutableTreeNode selNode = (DefaultMutableTreeNode) selTree
					.getLastPathComponent();
			Object obj = selNode.getUserObject();
			if (obj instanceof myObj) {
				outputText.setText(((myObj) obj).show + "  "
						+ ((myObj) obj).getDir().getDir_name());
			}
		}
	}

	public void mousePressed(java.awt.event.MouseEvent e) {}
	public void mouseReleased(java.awt.event.MouseEvent e) {}
	public void mouseEntered(java.awt.event.MouseEvent e) {}
	public void mouseExited(java.awt.event.MouseEvent e) {}
	public void keyTyped(java.awt.event.KeyEvent e) {}
	public void keyPressed(java.awt.event.KeyEvent e) {}
	public void keyReleased(java.awt.event.KeyEvent e) {}
	
	public void output_log(String msg){
		textArea_log.insert(DateUtils.formatDateTime(new Date()) + " " + msg+"\n", 0);
//		textArea_log.append(msg+"\n");
	}
	
	public static void showMessage(String mess){ 
		JOptionPane.showMessageDialog(null,
				mess, "ϵͳ��Ϣ", JOptionPane.INFORMATION_MESSAGE);
	}
	public static boolean isSelectYes(String info){
		int option = JOptionPane.showConfirmDialog(null,
				info, "info", JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE, null);
		return option == JOptionPane.YES_NO_OPTION;
	}

	
//	����һ��ʹ��JFileChooser�ؼ���
//
//	ʾ����
//
//	JFileChooser chooser = new JFileChooser();
//
//	chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);//����ֻ��ѡ��Ŀ¼
//	int returnVal = chooser.showOpenDialog(parent);
//	if(returnVal == JFileChooser.APPROVE_OPTION) {
//	  selectPath =chooser.getSelectedFile().getPath() ;
//
//	  System.out.println ( "��ѡ���Ŀ¼�ǣ�" + selectPath );
//	  chooser.hide();
//	}
//
//	 
//	��������ʹ��DirectoryDialog�ؼ���
//
//	ʾ����
//
//	Display display = new Display ();
//	Shell shell = new Shell (display);
//
//	 DirectoryDialog dialog = new DirectoryDialog (shell);
//	 selectPath = dialog.open() ;
//	 System.out.println ( "��ѡ���Ŀ¼�ǣ�" + selectPath );
//	 while (!shell.isDisposed()) {
//	        if (!display.readAndDispatch ()) display.sleep ();      
//	 }
//	 display.dispose ();

}
