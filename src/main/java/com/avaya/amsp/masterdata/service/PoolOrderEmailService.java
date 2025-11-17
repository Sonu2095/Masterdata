/**
 * 
 */
package com.avaya.amsp.masterdata.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.math3.util.Precision;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.avaya.amsp.domain.Currency;
import com.avaya.amsp.domain.PoolOrderArticle;
import com.avaya.amsp.domain.PoolOrderFiles;
import com.avaya.amsp.domain.PoolOrderItem;
import com.avaya.amsp.domain.UserDetails;
import com.avaya.amsp.masterdata.dtos.PoolOrderStatusCode;
import com.avaya.amsp.masterdata.repo.PoolOrderFilesRepo;
import com.avaya.amsp.masterdata.repo.PoolOrderRepository;
import com.avaya.amsp.masterdata.repo.UserDetailsRepo;
import com.avaya.amsp.masterdata.service.iface.PoolOrderEmailServiceIface;
import com.avaya.amsp.security.user.AMSPUser;
import com.avaya.amsp.service.mail.dto.EmailDTO;
import com.avaya.amsp.service.mail.iface.EmailService;
import com.avaya.amsp.shared.util.AMSPUtils;

//import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 */
@Service
@Slf4j
public class PoolOrderEmailService implements PoolOrderEmailServiceIface {

	@Autowired
	PoolOrderRepository poolOrderItemRepo;
	/*
	 * @Autowired OrderArticleRepo orderArticleRepo;
	 */
	@Autowired
	UserDetailsRepo userDetailsRepo;

	/*
	 * @Autowired EntityManager entityManager;
	 */

	@Autowired
	private MessageSource messageSource;

	@Autowired
	private TemplateEngine templateEngine;

	@Autowired
	private EmailService emailService;

	@Autowired
	private PoolOrderFilesRepo poolOrderFilesRepo;

	@Autowired
	ArticleClusterI18NNameService articleClusterI18NNameService;

	private static final String TMP_EMAIL_FILE_PATH = "/tmp/dbFile.pdf";
	private static final String PDF_EXTN = ".pdf";

	@Override
	@org.springframework.transaction.annotation.Transactional
	public void sendOrderOpenConfirmationEmail(AMSPUser user, PoolOrderItem orderItem, List<File> attachments) {

		/*
		 * if(!user.isEmailOnReqOpen()) {
		 * log.info("User {} has disabled email notifications for Order Open",
		 * user.getUsername()); return; }
		 */
		new Thread(new Runnable() {

			@Override
			@org.springframework.transaction.annotation.Transactional
			public void run() {
				try {

					Locale userLocale = AMSPUtils.getLocale(user.getDefaultLanguageId());
					log.info("Sending pool order open email to {} in language {}", user.getEmailAddress(),
							userLocale.getDisplayLanguage());
					String templateName = "pool-order-open_" + userLocale.getLanguage() + ".html";
					Context context = new Context(userLocale);

					// PoolOrderItem firstOrderItem = orderItem;
					Long orderNumber = orderItem.getId(); // OrderNumber
					String username = user.getUsername(); // username
					// String emailAddress = user.getEmailAddress(); // emailAddress
					// String currency = orderItem.getClusterItem().getArticleCurrency().getCode();

					LocalDate purchaseDate = orderItem.getPurchaseTs().toLocalDate(); // PurchaseDate
					LocalTime purchaseTime = orderItem.getPurchaseTs().toLocalTime(); // PurchaseTime

					String clusterName = String.valueOf(orderItem.getClusterItem().getName()); // Use cluster i18n name
																								// repo (need to be
																								// created)
					String siteName = String.valueOf(orderItem.getSite().getName()); // Use site i18n name repo (need to
																						// be created)
					String poolName = String.valueOf(orderItem.getPool().getName());
					String contractCode = String.valueOf(orderItem.getContractCode());

					// String department = firstOrderItem.getDepartment(); //Department
					// String costCenter = firstOrderItem.getCostCenter(); //CostCenter

					// int quantity = orderItem.size(); //Quantity

					Map<String, List<PoolArticleDetailsForEmail>> articleDetails = getArticleDetails(orderItem,
							user.getDefaultLanguageId()); // ArticleDetails

					UserDetails userDetails = userDetailsRepo.findById(username).get();

					String fName = !StringUtils.hasLength(userDetails.getFname()) ? username : userDetails.getFname();//
					String fullName = !StringUtils.hasLength(userDetails.getFname()) ? username
							: userDetails.getFname() + " " + userDetails.getLname();
					String uEmail = userDetails.getEmail();

					/*
					 * int totalMonthly = 0; int totalOneTime = 0;
					 * 
					 * for (Entry<String, List<PoolArticleDetailsForEmail>> e :
					 * articleDetails.entrySet()) { e.getKey(); e.getValue();
					 * 
					 * for (PoolArticleDetailsForEmail a : e.getValue()) { totalPuPrice +=
					 * a.getTotalPrice(); totalSaPrice += a.getTotalPrice(); }
					 * 
					 * context.setVariable(e.getKey() + "M", orderMonthly);
					 * context.setVariable(e.getKey() + "O", orderOneTime); }
					 */

					context.setVariable("fName", fName);
					context.setVariable("orderNum", orderNumber);
					context.setVariable("hotlinePh", "2626262"); // Get from Properties?
					context.setVariable("hotlinePin", "1234");// Get from Properties?
					context.setVariable("hotlineEml", "helpMe@avaya.com");// Get from Properties?
					context.setVariable("uEmail", uEmail);

					context.setVariable("orderDt", purchaseDate.format(DateTimeFormatter.ofPattern("dd.MM.YYYY")));
					context.setVariable("orderTm", purchaseTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")));

					context.setVariable("fullName", fullName);

					context.setVariable("clusterName", clusterName);
					context.setVariable("siteName", siteName);
					context.setVariable("contractCode", contractCode);
					context.setVariable("poolName", poolName);

					// context.setVariable("quantity", quantity);
					// context.setVariable("remark", remark);

					context.setVariable("articleDetails", articleDetails);
					context.setVariable("orders", articleDetails);

					String body = templateEngine.process(templateName, context);

					// Store the file in DB.
					storeFileToDB(body, orderItem, PoolOrderStatusCode.OPENED.name());

					emailService.sendEmail(EmailDTO.builder().from("no-reply@avaya.com")
							.to(List.of(user.getEmailAddress()))
							.subject(messageSource.getMessage("email.subject.order.open",
									new Object[] { String.valueOf(orderNumber), username, clusterName, poolName },
									userLocale))
							.body(body).build());
					log.debug("Task submitted to send email to {}", user.getEmailAddress());

				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}).start();

	}

	@Override
	public void sendOrderApprovedConfirmationEmail(AMSPUser user, PoolOrderItem orderItem, List<File> attachments) {

		new Thread(new Runnable() {

			@Override
			@org.springframework.transaction.annotation.Transactional
			public void run() {
				try {

					Locale userLocale = AMSPUtils.getLocale(user.getDefaultLanguageId());
					log.info("Sending pool order approved email to {} in language {}", user.getEmailAddress(),
							userLocale.getDisplayLanguage());
					String templateName = "pool-order-approved_" + userLocale.getLanguage() + ".html";
					Context context = new Context(userLocale);

					// PoolOrderItem firstOrderItem = orderItem;
					Long orderNumber = orderItem.getId(); // OrderNumber
					String username = user.getUsername(); // username
					// String emailAddress = user.getEmailAddress(); // emailAddress
					// String currency = orderItem.getClusterItem().getArticleCurrency().getCode();

					LocalDate purchaseDate = orderItem.getPurchaseTs().toLocalDate(); // PurchaseDate
					LocalTime purchaseTime = orderItem.getPurchaseTs().toLocalTime(); // PurchaseTime

					LocalDate approvedDate = orderItem.getApproveTs().toLocalDate(); // ApprovedDate
					LocalTime approvedTime = orderItem.getApproveTs().toLocalTime(); // ApprovedTime

					String clusterName = String.valueOf(orderItem.getClusterItem().getName()); // Use cluster i18n name
																								// repo (need to be
																								// created)
					String siteName = String.valueOf(orderItem.getSite().getName()); // Use site i18n name repo (need to
																						// be created)
					String poolName = String.valueOf(orderItem.getPool().getName());
					String contractCode = String.valueOf(orderItem.getContractCode());

					String shippingAddress = String.valueOf(orderItem.getShippingAddress().getShippingAddressString());

					// String department = firstOrderItem.getDepartment(); //Department
					// String costCenter = firstOrderItem.getCostCenter(); //CostCenter

					// int quantity = orderItem.size(); //Quantity

					Map<String, List<PoolArticleDetailsForEmail>> articleDetails = getArticleDetails(orderItem,
							user.getDefaultLanguageId()); // ArticleDetails

					UserDetails userDetails = userDetailsRepo.findById(username).get();

					String fName = !StringUtils.hasLength(userDetails.getFname()) ? username : userDetails.getFname();//
					String fullName = !StringUtils.hasLength(userDetails.getFname()) ? username
							: userDetails.getFname() + " " + userDetails.getLname();
					String uEmail = userDetails.getEmail();

					/*
					 * int totalMonthly = 0; int totalOneTime = 0;
					 * 
					 * for (Entry<String, List<PoolArticleDetailsForEmail>> e :
					 * articleDetails.entrySet()) { e.getKey(); e.getValue();
					 * 
					 * for (PoolArticleDetailsForEmail a : e.getValue()) { totalPuPrice +=
					 * a.getTotalPrice(); totalSaPrice += a.getTotalPrice(); }
					 * 
					 * context.setVariable(e.getKey() + "M", orderMonthly);
					 * context.setVariable(e.getKey() + "O", orderOneTime); }
					 */

					context.setVariable("fName", fName);
					context.setVariable("orderNum", orderNumber);
					context.setVariable("hotlinePh", "2626262"); // Get from Properties?
					context.setVariable("hotlinePin", "1234");// Get from Properties?
					context.setVariable("hotlineEml", "helpMe@avaya.com");// Get from Properties?
					context.setVariable("uEmail", uEmail);

					context.setVariable("orderDt", purchaseDate.format(DateTimeFormatter.ofPattern("dd.MM.YYYY")));
					context.setVariable("orderTm", purchaseTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")));

					context.setVariable("approvedDt", approvedDate.format(DateTimeFormatter.ofPattern("dd.MM.YYYY")));
					context.setVariable("approvedTm", approvedTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")));

					context.setVariable("fullName", fullName);

					context.setVariable("clusterName", clusterName);
					context.setVariable("siteName", siteName);
					context.setVariable("contractCode", contractCode);
					context.setVariable("poolName", poolName);

					context.setVariable("shippingAddress", shippingAddress);

					// context.setVariable("quantity", quantity);
					// context.setVariable("remark", remark);

					context.setVariable("articleDetails", articleDetails);
					context.setVariable("orders", articleDetails);

					String body = templateEngine.process(templateName, context);

					// Store the file in DB.
					storeFileToDB(body, orderItem, PoolOrderStatusCode.WAITING_SAP.name());

					emailService.sendEmail(EmailDTO.builder().from("no-reply@avaya.com")
							.to(List.of(user.getEmailAddress()))
							.subject(messageSource.getMessage("email.subject.order.approved",
									new Object[] { String.valueOf(orderNumber), username, clusterName, poolName },
									userLocale))
							.body(body).build());
					log.debug("Task submitted to send email to {}", user.getEmailAddress());

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	@Override
	public void sendOrderDeliveryRequestEmail(AMSPUser user, PoolOrderItem orderItem, List<File> attachments) {
		new Thread(new Runnable() {

			@Override
			@org.springframework.transaction.annotation.Transactional
			public void run() {
				try {

					Locale userLocale = AMSPUtils.getLocale(user.getDefaultLanguageId());
					log.info("Sending pool order delivery request email to {} in language {}", user.getEmailAddress(),
							userLocale.getDisplayLanguage());
					String templateName = "pool-order-delivery-req_" + userLocale.getLanguage() + ".html";
					Context context = new Context(userLocale);

					// PoolOrderItem firstOrderItem = orderItem;
					Long orderNumber = orderItem.getId(); // OrderNumber
					String username = user.getUsername(); // username
					// String emailAddress = user.getEmailAddress(); // emailAddress
					// String currency = orderItem.getClusterItem().getArticleCurrency().getCode();

					LocalDate purchaseDate = orderItem.getPurchaseTs().toLocalDate(); // PurchaseDate
					LocalTime purchaseTime = orderItem.getPurchaseTs().toLocalTime(); // PurchaseTime

					LocalDate sapDt = orderItem.getSapTs().toLocalDate(); // SapDate

					String clusterName = String.valueOf(orderItem.getClusterItem().getName()); // Use cluster i18n name
																								// repo (need to be
																								// created)
					String siteName = String.valueOf(orderItem.getSite().getName()); // Use site i18n name repo (need to
																						// be created)
					String poolName = String.valueOf(orderItem.getPool().getName());
					String contractCode = String.valueOf(orderItem.getContractCode());

					String shippingAddress = String.valueOf(orderItem.getShippingAddress().getShippingAddressString());

					// String department = firstOrderItem.getDepartment(); //Department
					// String costCenter = firstOrderItem.getCostCenter(); //CostCenter

					// int quantity = orderItem.size(); //Quantity

					Map<String, List<PoolArticleDetailsForEmail>> articleDetails = getArticleDetails(orderItem,
							user.getDefaultLanguageId()); // ArticleDetails

					UserDetails userDetails = userDetailsRepo.findById(username).get();

					String fName = !StringUtils.hasLength(userDetails.getFname()) ? username : userDetails.getFname();//
					String fullName = !StringUtils.hasLength(userDetails.getFname()) ? username
							: userDetails.getFname() + " " + userDetails.getLname();
					String uEmail = userDetails.getEmail();

					/*
					 * int totalMonthly = 0; int totalOneTime = 0;
					 * 
					 * for (Entry<String, List<PoolArticleDetailsForEmail>> e :
					 * articleDetails.entrySet()) { e.getKey(); e.getValue();
					 * 
					 * for (PoolArticleDetailsForEmail a : e.getValue()) { totalPuPrice +=
					 * a.getTotalPrice(); totalSaPrice += a.getTotalPrice(); }
					 * 
					 * context.setVariable(e.getKey() + "M", orderMonthly);
					 * context.setVariable(e.getKey() + "O", orderOneTime); }
					 */

					context.setVariable("fName", fName);
					context.setVariable("orderNum", orderNumber);
					context.setVariable("hotlinePh", "2626262"); // Get from Properties?
					context.setVariable("hotlinePin", "1234");// Get from Properties?
					context.setVariable("hotlineEml", "helpMe@avaya.com");// Get from Properties?
					context.setVariable("uEmail", uEmail);

					context.setVariable("orderDt", purchaseDate.format(DateTimeFormatter.ofPattern("dd.MM.YYYY")));
					context.setVariable("orderTm", purchaseTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")));

					context.setVariable("sapDt", sapDt.format(DateTimeFormatter.ofPattern("dd.MM.YYYY")));

					context.setVariable("fullName", fullName);

					context.setVariable("clusterName", clusterName);
					context.setVariable("siteName", siteName);
					context.setVariable("contractCode", contractCode);
					context.setVariable("poolName", poolName);

					context.setVariable("shippingAddress", shippingAddress);

					// context.setVariable("quantity", quantity);
					// context.setVariable("remark", remark);

					context.setVariable("articleDetails", articleDetails);
					context.setVariable("orders", articleDetails);

					String body = templateEngine.process(templateName, context);

					// Store the file in DB.
					storeFileToDB(body, orderItem, PoolOrderStatusCode.WAITING_SHIPPING.name());

					emailService.sendEmail(EmailDTO.builder().from("no-reply@avaya.com")
							.to(List.of(user.getEmailAddress()))
							.subject(messageSource.getMessage("email.subject.order.delivery.request",
									new Object[] { String.valueOf(orderNumber), username, clusterName, poolName },
									userLocale))
							.body(body).build());
					log.debug("Task submitted to send email to {}", user.getEmailAddress());

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	@Override
	public void sendOrderClosedConfirmationEmail(AMSPUser user, PoolOrderItem orderItem, List<File> attachments) {
		new Thread(new Runnable() {

			@Override
			@org.springframework.transaction.annotation.Transactional
			public void run() {
				try {

					Locale userLocale = AMSPUtils.getLocale(user.getDefaultLanguageId());
					log.info("Sending pool order delivery email to {} in language {}", user.getEmailAddress(),
							userLocale.getDisplayLanguage());
					String templateName = "pool-order-closed_" + userLocale.getLanguage() + ".html";
					Context context = new Context(userLocale);

					// PoolOrderItem firstOrderItem = orderItem;
					Long orderNumber = orderItem.getId(); // OrderNumber
					String username = user.getUsername(); // username
					// String emailAddress = user.getEmailAddress(); // emailAddress
					// String currency = orderItem.getClusterItem().getArticleCurrency().getCode();

					LocalDate purchaseDate = orderItem.getPurchaseTs().toLocalDate(); // PurchaseDate
					LocalTime purchaseTime = orderItem.getPurchaseTs().toLocalTime(); // PurchaseTime

					LocalDate deliveredDt = orderItem.getShippingTs(); // DeliveryDate

					String clusterName = String.valueOf(orderItem.getClusterItem().getName()); // Use cluster i18n name
																								// repo (need to be
																								// created)
					String siteName = String.valueOf(orderItem.getSite().getName()); // Use site i18n name repo (need to
																						// be created)
					String poolName = String.valueOf(orderItem.getPool().getName());
					String contractCode = String.valueOf(orderItem.getContractCode());

					String shippingAddress = String.valueOf(orderItem.getShippingAddress().getShippingAddressString());

					// String department = firstOrderItem.getDepartment(); //Department
					// String costCenter = firstOrderItem.getCostCenter(); //CostCenter

					// int quantity = orderItem.size(); //Quantity

					Map<String, List<PoolArticleDetailsForEmail>> articleDetails = getArticleDetails(orderItem,
							user.getDefaultLanguageId()); // ArticleDetails

					UserDetails userDetails = userDetailsRepo.findById(username).get();

					String fName = !StringUtils.hasLength(userDetails.getFname()) ? username : userDetails.getFname();//
					String fullName = !StringUtils.hasLength(userDetails.getFname()) ? username
							: userDetails.getFname() + " " + userDetails.getLname();
					String uEmail = userDetails.getEmail();

					/*
					 * int totalMonthly = 0; int totalOneTime = 0;
					 * 
					 * for (Entry<String, List<PoolArticleDetailsForEmail>> e :
					 * articleDetails.entrySet()) { e.getKey(); e.getValue();
					 * 
					 * for (PoolArticleDetailsForEmail a : e.getValue()) { totalPuPrice +=
					 * a.getTotalPrice(); totalSaPrice += a.getTotalPrice(); }
					 * 
					 * context.setVariable(e.getKey() + "M", orderMonthly);
					 * context.setVariable(e.getKey() + "O", orderOneTime); }
					 */

					context.setVariable("fName", fName);
					context.setVariable("orderNum", orderNumber);
					context.setVariable("hotlinePh", "2626262"); // Get from Properties?
					context.setVariable("hotlinePin", "1234");// Get from Properties?
					context.setVariable("hotlineEml", "helpMe@avaya.com");// Get from Properties?
					context.setVariable("uEmail", uEmail);

					context.setVariable("orderDt", purchaseDate.format(DateTimeFormatter.ofPattern("dd.MM.YYYY")));
					context.setVariable("orderTm", purchaseTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")));

					context.setVariable("deliveredDt", deliveredDt.format(DateTimeFormatter.ofPattern("dd.MM.YYYY")));
					// context.setVariable("completionTm",
					// purchaseTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")));

					// context.setVariable("deliveredOn",
					// deliveredOn.format(DateTimeFormatter.ofPattern("dd.MM.YYYY")));

					context.setVariable("fullName", fullName);

					context.setVariable("clusterName", clusterName);
					context.setVariable("siteName", siteName);
					context.setVariable("contractCode", contractCode);
					context.setVariable("poolName", poolName);

					context.setVariable("shippingAddress", shippingAddress);

					// context.setVariable("quantity", quantity);
					// context.setVariable("remark", remark);

					context.setVariable("articleDetails", articleDetails);
					context.setVariable("orders", articleDetails);

					String body = templateEngine.process(templateName, context);

					// Store the file in DB.
					storeFileToDB(body, orderItem, PoolOrderStatusCode.CLOSED.name());

					emailService.sendEmail(EmailDTO.builder().from("no-reply@avaya.com")
							.to(List.of(user.getEmailAddress()))
							.subject(messageSource.getMessage("email.subject.order.closed",
									new Object[] { String.valueOf(orderNumber), username, clusterName, poolName },
									userLocale))
							.body(body).build());
					log.debug("Task submitted to send email to {}", user.getEmailAddress());

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	private Map<String, List<PoolArticleDetailsForEmail>> getArticleDetails(PoolOrderItem orderItem,
			String languageId) {
		Map<String, List<PoolArticleDetailsForEmail>> articleDetailsMap = new TreeMap<>();
		List<PoolArticleDetailsForEmail> articleDetails = null;
		articleDetails = new ArrayList<>(orderItem.getPoolOrderArticles().size());

		Currency currency = orderItem.getClusterItem().getArticleCurrency();
		String currencySymbol = currency.getSymbol();

		Double saPrice = 0.0;
		Double puPrice = 0.0;
		Double totalSaPrice = 0.0;
		Double totalPuPrice = 0.0;
		long articleQuantity;

		for (PoolOrderArticle orderArticle : orderItem.getPoolOrderArticles()) {
			String articleName = articleClusterI18NNameService.getTranslation(orderArticle.getClusterArticleId(),
					languageId, orderArticle.getArticleCluster().getName());
			articleQuantity = orderArticle.getQuantity();

			switch (currency.getCode()) {
			case "EUR":
				saPrice = orderArticle.getArticleCluster().getPriceSalesEuro();
				puPrice = orderArticle.getArticleCluster().getPricePurchaseEuro();
				break;
			case "USD":
				saPrice = orderArticle.getArticleCluster().getPriceSalesDollar();
				puPrice = orderArticle.getArticleCluster().getPricePurchaseDollar();
			}

			totalSaPrice = Precision.round(articleQuantity * saPrice, 2);
			totalPuPrice = Precision.round(articleQuantity * puPrice, 2);

			/*
			 * totalSaPrice = (float) (articleQuantity * saPrice); totalPuPrice = (float)
			 * (articleQuantity * puPrice);
			 */

			articleDetails.add(new PoolArticleDetailsForEmail(articleName, articleQuantity,
					saPrice.toString() + currencySymbol, puPrice.toString() + currencySymbol,
					totalSaPrice.toString() + currencySymbol, totalPuPrice.toString() + currencySymbol));
		}
		articleDetailsMap.put(orderItem.getId().toString(), articleDetails);
		return articleDetailsMap;
	}

	private void storeFileToDB(String body, PoolOrderItem orderItem, String orderStatus) throws IOException {
		OutputStream outputStream = new FileOutputStream(TMP_EMAIL_FILE_PATH);

		ITextRenderer renderer = new ITextRenderer();
		renderer.setDocumentFromString(body);
		renderer.layout();
		renderer.createPDF(outputStream);
		outputStream.close();

		// Logic to generate file hash
		byte[] fileContent = getByteArrayFromFile(TMP_EMAIL_FILE_PATH);
		String fileHash = null;
		try {
			byte[] bytes = MessageDigest.getInstance("SHA3-256").digest(fileContent);
			StringBuilder sb = new StringBuilder();
			for (byte b : bytes) {
				sb.append(String.format("%02x", b));
			}
			fileHash = sb.toString();
		} catch (NoSuchAlgorithmException e) {
			log.error("Error while generating file hash. Using random number", e);
			fileHash = Math.random() + "";
		}

		PoolOrderFiles pof = new PoolOrderFiles();
		pof.setFileId(fileHash);
		pof.setPoolOrderStatus(orderStatus);
		pof.setContent(fileContent);
		pof.setPoolOrderItem(orderItem);
		pof.setPoolOrderItemId(orderItem.getId());
		pof.setFileName(orderItem.getId() + "_" + orderStatus + PDF_EXTN);
		pof.setFileSize(pof.getContent().length);
		pof.setUploadTs(LocalDateTime.now());

		poolOrderFilesRepo.save(pof);
	}

	private byte[] getByteArrayFromFile(String filePath) throws IOException {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final InputStream in = new FileInputStream(filePath);
		final byte[] buffer = new byte[500];

		int read = -1;
		while ((read = in.read(buffer)) > 0) {
			baos.write(buffer, 0, read);
		}
		in.close();

		return baos.toByteArray();
	}
}
