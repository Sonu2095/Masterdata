package com.avaya.amsp.masterdata.rest;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.avaya.amsp.masterdata.dtos.ArticleToClusterDto;
import com.avaya.amsp.masterdata.dtos.ConnectionDto;
import com.avaya.amsp.masterdata.dtos.SubArticleDetailDto;
import com.avaya.amsp.masterdata.service.iface.ArticleClusterServiceIface;
import com.avaya.amsp.security.user.AMSPUser;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/v1/articlecluster")
public class ArticleClusterController {

	@Autowired
	private ArticleClusterServiceIface articleClusterService;

	@GetMapping("")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> fetchClusterArticles() {

		List<ArticleToClusterDto> response = new ArrayList<>();
		log.info("request received to fetch cluster article from database");
		response = articleClusterService.fetchAllArticles();
		return ResponseEntity.status(HttpStatus.OK).body(response);

	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<String> updateArticle(@RequestBody ArticleToClusterDto request,
			@PathVariable("id") Long articleId, @AuthenticationPrincipal AMSPUser amspUser) {

		log.info("request received to update article cluster to database {}", request);
		request.setUser(amspUser.getUsername());
		request.setLogUpdatedBy(amspUser.getUsername());
		// request.setArticleId(articleId);
		articleClusterService.updateArticle(request);
		return ResponseEntity.status(HttpStatus.OK).body("");

	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<String> deleteArticle(@PathVariable("id") Long articleId, @AuthenticationPrincipal AMSPUser amspUser) {

		log.info("request received to delete article cluster from DB with Id as {}", articleId);
		articleClusterService.removeArticle(articleId, amspUser.getUsername());
		return ResponseEntity.status(HttpStatus.NO_CONTENT).body("");
	}

	/**
	 * This method will be used to get the connections for an Article
	 * 
	 * @return : 200 OK
	 */

	@GetMapping("/{id}/connections")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> fetchConnectionsByArticle(@PathVariable("id") Long articleId) {
		log.info("Request received to fetch all connections for article {}", articleId);
		List<ConnectionDto> connections = articleClusterService.fetchConnectionsByArticle(articleId);
		return ResponseEntity.status(HttpStatus.OK).body(connections);
	}

	@GetMapping("articles/{articleId}/clusters/{clusterId}/")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> fetchRecordByClusterArticle(@PathVariable("articleId") Long articleId,
			@PathVariable("clusterId") Long clusterId) {
		log.info("Request received to fetch article cluster by clusterId and master Article Id {}", articleId);
		List<ArticleToClusterDto> records = articleClusterService.fetchRecordByArticleCluster(articleId, clusterId);
		return ResponseEntity.status(HttpStatus.OK).body(records);
	}

	@PostMapping("/{id}/subarticles")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<String> addSubArticlesToLeadArticle(@PathVariable("id") Long articleId,
			@RequestBody @Valid SubArticleDetailDto subArticles,@AuthenticationPrincipal AMSPUser amspUser) {
		log.info("request received to add sub articles {} to lead articleId {} for articlecluster subarticle",
				subArticles, articleId);
		articleClusterService.addSubArticlesToArticle(articleId, subArticles, amspUser.getUsername());
		return ResponseEntity.status(HttpStatus.OK).body("");
	}

	@DeleteMapping("/{id}/subarticles")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<String> deleteSubArticles(@PathVariable("id") Long articleId,
			@RequestBody @Valid SubArticleDetailDto subArticlesDto, @AuthenticationPrincipal AMSPUser amspUser) {

		log.info("request received to delete subarticle for a lead article for cluster partlist {}", articleId);
		articleClusterService.deleteAssignSubArticles(articleId, subArticlesDto, amspUser.getUsername());
		return ResponseEntity.status(HttpStatus.NO_CONTENT).body("");
	}

	@GetMapping("/{leadArticleId}/subarticles")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> fetchClusterSubArticles(@PathVariable("leadArticleId") Long leadArticleId) {
		List<ArticleToClusterDto> response = new ArrayList<>();
		log.info("request received to fetch cluster partlist sub articles for a lead article");
		response = articleClusterService.fetchClusterSubArticles(leadArticleId);
		return ResponseEntity.status(HttpStatus.OK).body(response);

	}

	@PutMapping("/{id}/removemasterpartstatus")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<String> removeMasterPartStatus(@PathVariable("id") Long articleId, @AuthenticationPrincipal AMSPUser amspUser) {
		log.info("request received to remove master part status for articleId {} ", articleId);
		articleClusterService.removeMasterPartStatus(articleId, amspUser.getUsername());
		return ResponseEntity.status(HttpStatus.OK).body("");
	}

	@DeleteMapping("/partlist/leadarticle{id}")
	@PreAuthorize("hasRole('AVAYA_ADMIN')")
	public ResponseEntity<String> deleteClusterPartlistLeadArticle(@PathVariable("id") Long leadId, @AuthenticationPrincipal AMSPUser amspUser) {
		log.info("request received to delete a cluster partlist lead article {}", leadId);
		articleClusterService.deleteClusterPartListLeadArticles(leadId, amspUser.getUsername());
		return ResponseEntity.status(HttpStatus.NO_CONTENT).body("");
	}

}
