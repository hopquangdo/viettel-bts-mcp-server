package vn.edu.huce.iic.bts_ops_platform.modules.core.file.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.huce.iic.bts_ops_platform.infrastructure.storage.LocalFileStorageService;
import vn.edu.huce.iic.bts_ops_platform.infrastructure.storage.StoredFile;
import vn.edu.huce.iic.bts_ops_platform.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.common.util.EntityFilter;
import vn.edu.huce.iic.bts_ops_platform.common.util.SecurityContextHelper;
import vn.edu.huce.iic.bts_ops_platform.modules.core.file.dto.request.TepDinhKemCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.file.dto.request.TepDinhKemTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.file.dto.response.TepDinhKemResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.file.entity.TepDinhKem;
import vn.edu.huce.iic.bts_ops_platform.modules.core.file.exception.FileErrorCode;
import vn.edu.huce.iic.bts_ops_platform.modules.core.file.mapper.FileMapper;
import vn.edu.huce.iic.bts_ops_platform.modules.core.file.repository.TepDinhKemRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.file.services.TepDinhKemService;

import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TepDinhKemServiceImpl implements TepDinhKemService {

    private final TepDinhKemRepository tepDinhKemRepository;
    private final FileMapper fileMapper;
    private final LocalFileStorageService localFileStorageService;

    @Override
    @Transactional(readOnly = true)
    public TepDinhKemResponse getById(UUID id) {
        return fileMapper.toTepDinhKemResponse(findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, TepDinhKemResponse> getByIds(Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        return tepDinhKemRepository.findByIdInAndNgayXoaIsNull(ids.stream().distinct().toList()).stream()
                .collect(Collectors.toMap(TepDinhKem::getId, fileMapper::toTepDinhKemResponse, (a, b) -> a));
    }

    @Override
    @Transactional
    public TepDinhKemResponse create(TepDinhKemTaoRequest request) {
        TepDinhKem entity = fileMapper.fromTepDinhKemTaoRequest(request);
        entity.setMa(EntityFilter.normalizeCode(request.getMa()));
        if (request.getTenTep() != null) entity.setTenTep(request.getTenTep().trim());
        if (request.getTenTepGoc() != null) entity.setTenTepGoc(request.getTenTepGoc().trim());
        if (request.getDuongDan() != null) entity.setDuongDan(request.getDuongDan().trim());
        if (request.getUrl() != null) entity.setUrl(request.getUrl().trim());
        if (request.getLoaiTep() != null) entity.setLoaiTep(request.getLoaiTep().trim());
        if (request.getMimeType() != null) entity.setMimeType(request.getMimeType().trim());
        if (request.getChecksum() != null) entity.setChecksum(request.getChecksum().trim());
        entity.setHoatDong(request.getHoatDong() == null || request.getHoatDong());
        entity.setNguoiTaoId(SecurityContextHelper.requireCurrentUser().id());

        return fileMapper.toTepDinhKemResponse(tepDinhKemRepository.save(entity));
    }

    @Override
    @Transactional
    public TepDinhKemResponse upload(MultipartFile file, String category) {
        StoredFile stored = localFileStorageService.store(file, category);
        TepDinhKem entity = new TepDinhKem();
        entity.setTenTep(stored.storedFileName());
        entity.setTenTepGoc(stored.originalFileName());
        entity.setDuongDan(stored.relativePath());
        entity.setLoaiTep(stored.extension());
        entity.setMimeType(stored.contentType());
        entity.setKichThuoc(stored.size());
        entity.setHoatDong(true);
        entity.setNguoiTaoId(SecurityContextHelper.requireCurrentUser().id());
        return fileMapper.toTepDinhKemResponse(tepDinhKemRepository.save(entity));
    }

    @Override
    @Transactional
    public TepDinhKemResponse update(UUID id, TepDinhKemCapNhatRequest request) {
        TepDinhKem entity = findById(id);
        fileMapper.updateFromTepDinhKemCapNhatRequest(request, entity);

        return fileMapper.toTepDinhKemResponse(tepDinhKemRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        TepDinhKem entity = findById(id);
        entity.setNgayXoa(Instant.now());
        entity.setHoatDong(false);
        tepDinhKemRepository.save(entity);
    }

    private TepDinhKem findById(UUID id) {
        return tepDinhKemRepository.findByIdAndNgayXoaIsNull(id)
                .orElseThrow(() -> new AppException(FileErrorCode.TEP_DINH_KEM_NOT_FOUND, "Không tìm thấy bản ghi"));
    }
}
