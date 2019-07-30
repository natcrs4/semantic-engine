package com.crs4.sem.search.bridges;

import org.hibernate.search.bridge.ContainerBridge;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.builtin.IterableBridge;


public class IterableMetadataBridge extends IterableBridge implements ContainerBridge {
	private final FieldBridge bridge;

	private static final MetadataBridge DEFAULT_BRIDGE = MetadataBridge.INSTANCE;

	public IterableMetadataBridge(final FieldBridge fieldBridge) {
		super(fieldBridge);
		if ( fieldBridge instanceof MetadataBridge ) {
			this.bridge = (MetadataBridge) fieldBridge;
		}
		else {
			this.bridge = DEFAULT_BRIDGE;
		}
	}

	public IterableMetadataBridge() {

		this(MetadataBridge.INSTANCE);
	}
}