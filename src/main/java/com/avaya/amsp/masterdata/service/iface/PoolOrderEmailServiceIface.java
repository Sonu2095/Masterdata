/**
 * 
 */
package com.avaya.amsp.masterdata.service.iface;

import java.io.File;
import java.util.List;

import com.avaya.amsp.domain.PoolOrderItem;
import com.avaya.amsp.security.user.AMSPUser;

/**
 * 
 */
public interface PoolOrderEmailServiceIface {
	
	void sendOrderOpenConfirmationEmail(AMSPUser user, PoolOrderItem orderItems, List<File> attachments);
	
	void sendOrderApprovedConfirmationEmail(AMSPUser user, PoolOrderItem orderItem, List<File> attachments);
	
	void sendOrderClosedConfirmationEmail(AMSPUser user, PoolOrderItem orderItem, List<File> attachments);

	void sendOrderDeliveryRequestEmail(AMSPUser user, PoolOrderItem orderItem, List<File> attachments);

}
