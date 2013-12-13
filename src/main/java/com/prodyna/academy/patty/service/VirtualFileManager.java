/**
 * 
 */
package com.prodyna.academy.patty.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.prodyna.academy.patty.domain.FilesystemAbstractFactory;
import com.prodyna.academy.patty.domain.Folder;
import com.prodyna.academy.patty.domain.Node;

/**
 * implements a file manager as a singleton.
 * 
 * @author aheizenreder
 *
 */
public class VirtualFileManager implements FileManager {

	private static FileManager fileManager;
	
	private final Map<Node, List<NodeObserver>> observers = new HashMap<Node,List<NodeObserver>>();
	
	private final Folder root;
	
	public synchronized static FileManager getInstance() {
		if(fileManager == null) {
			fileManager = new VirtualFileManager();
		}
		return fileManager;
	}
	
	private VirtualFileManager() {
		root = FilesystemAbstractFactory.createFolder("/");
	}
	
	/* (non-Javadoc)
	 * @see com.prodyna.academy.patty.domain.NodeObserver#notifyObserver()
	 */
	@Override
	public void notifyObserver(final Node aNode) {
		final List<NodeObserver> observerList = observers.get(aNode);
		
		if(observerList != null) {
			for (final NodeObserver nodeObserver : observerList) {
				nodeObserver.notify();
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.prodyna.academy.patty.domain.FileManager#getRoot()
	 */
	@Override
	public Folder getRoot() {
		return root;
	}

	/* (non-Javadoc)
	 * @see com.prodyna.academy.patty.domain.FileManager#newNode(com.prodyna.academy.patty.domain.FileType)
	 */
	@Override
	public Node newTextFileNode(final FileType fType, final String name, final long size, final String textEncoding, final long pageCount) throws UnsupportedFileType {
		Node node;
		
		switch(fType) {
		case TEXT:
			node = FilesystemAbstractFactory.createTextFile(name, size, textEncoding, pageCount);
			break;
		default:
			throw new UnsupportedFileType();
		}
		
		return node;
	}
	
	/* (non-Javadoc)
	 * @see com.prodyna.academy.patty.domain.FileManager#newNode(com.prodyna.academy.patty.domain.FileType)
	 */
	@Override
	public Node newMediaFileNode(final FileType fType, final String name, final long size, final long height, final long width) throws UnsupportedFileType {
		Node node;
		
		switch(fType) {
		case IMAGE:
			node = FilesystemAbstractFactory.createImageFile(name, size, height, width);
			break;
		case VIDEO:
			node = FilesystemAbstractFactory.createVideoFile(name, size, height, width);
			break;
		default:
			throw new UnsupportedFileType();
		}
		
		return node;
	}
	
	@Override
	public Node newFolderNode(final FileType fType, final String name) throws UnsupportedFileType {
		Node node;
		
		switch(fType) {
		case FOLDER:
			node = FilesystemAbstractFactory.createFolder(name);
			break;
		default:
			throw new UnsupportedFileType();
		}
		
		return node;
	}

	/* (non-Javadoc)
	 * @see com.prodyna.academy.patty.domain.FileManager#add(com.prodyna.academy.patty.domain.Folder, com.prodyna.academy.patty.domain.Node)
	 */
	@Override
	public Node add(final Folder parent, final Node aNode) {
		parent.add(aNode);
		notifyObserver(parent);
		return aNode;
	}

	/* (non-Javadoc)
	 * @see com.prodyna.academy.patty.domain.FileManager#delete(com.prodyna.academy.patty.domain.Node)
	 */
	@Override
	public Node delete(final Node aNode) {
		aNode.delete();
		notifyObserver(aNode.getParent());
		return aNode;
	}

	/* (non-Javadoc)
	 * @see com.prodyna.academy.patty.domain.FileManager#list(com.prodyna.academy.patty.domain.Node)
	 */
	@Override
	public List<Node> list(final Node aNode) {
		return aNode.list();
	}

	/* (non-Javadoc)
	 * @see com.prodyna.academy.patty.domain.FileManager#register(com.prodyna.academy.patty.domain.NodeObserver, com.prodyna.academy.patty.domain.Node)
	 */
	@Override
	public void register(final NodeObserver aObserver, final Node aNode) {
		List<NodeObserver> observerList = observers.get(aNode);
		
		if(observerList == null) {
			observerList = new ArrayList<NodeObserver>();
			observers.put(aNode, observerList);
		}

		observerList.add(aObserver);
	}

	/* (non-Javadoc)
	 * @see com.prodyna.academy.patty.domain.FileManager#unRegister(com.prodyna.academy.patty.domain.NodeObserver)
	 */
	@Override
	public void unRegister(final NodeObserver aObserver, final Node aNode) {
		final List<NodeObserver> observerList = observers.get(aNode);
		
		if(observerList != null) {
			observerList.remove(aObserver);
		}
	}

	@Override
	public Node move(final Node aNode, final Folder newParent) {
		final Folder oldParentFolder = aNode.getParent();
		oldParentFolder.deleteChild(aNode);
		
		newParent.add(aNode);
		
		return aNode;
	}
	
	@Override
	public int getImageFileAmount(Node aNode) {
		CountVisitor visitor = new CountVisitor();
		visitor.setFilter(new ImageFilter());
		
		aNode.accept(visitor);
		
		return visitor.getCountResult();
	}
	
	@Override
	public int getFileAmount(Node aNode) {
		CountVisitor visitor = new CountVisitor();
		visitor.setFilter(new AllFileFilter());
		
		aNode.accept(visitor);
		
		return visitor.getCountResult();
	}
	
	@Override
	public String findByFileName(Folder aNode, String fileName) {
		PrintVisitor visitor = new PrintVisitor();
		visitor.setFilter(new FilenameFilter(fileName));
		
		aNode.accept(visitor);
		
		return visitor.getResultList();
	}
}