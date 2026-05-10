import os
import platform

# Guard: CUDA DLL lookup only works on Windows
if os.name == 'nt':
    _cuda_base = os.environ.get("CUDA_PATH", r"C:\Program Files\NVIDIA GPU Computing Toolkit\CUDA\v12.6")
    _cuda_bin = os.path.join(_cuda_base, "bin")
    if os.path.isdir(_cuda_bin):
        os.add_dll_directory(_cuda_bin)
        if _cuda_bin not in os.environ.get("PATH", ""):
            os.environ["PATH"] = _cuda_bin + ";" + os.environ.get("PATH", "")

# Import faster-whisper only if on Windows or if CUDA is available
_asr_available = False
ASR_ENABLED = os.getenv("ASR_ENABLED", "true").lower() != "false"

if ASR_ENABLED:
    try:
        from faster_whisper import WhisperModel
        _asr_available = True
    except (ImportError, OSError) as e:
        _asr_available = False
        print(f"Warning: faster-whisper not available: {e}")


class ASRService:
    def __init__(self):
        self._model = None

    @property
    def model(self):
        if not _asr_available:
            raise RuntimeError("ASR service not available (faster-whisper not installed or GPU not accessible)")
        if self._model is None:
            self._model = WhisperModel(
                "medium",
                device="cuda" if os.name == 'nt' else "cpu",
                compute_type="float16" if os.name == 'nt' else "int8",
            )
        return self._model

    async def transcribe(self, audio_bytes: bytes) -> str:
        if not _asr_available:
            raise RuntimeError("ASR service not available")
        import tempfile
        tmp = tempfile.NamedTemporaryFile(suffix=".wav", delete=False)
        try:
            tmp.write(audio_bytes)
            tmp.close()
            segments, _ = self.model.transcribe(tmp.name, language=None)
            text = " ".join(seg.text.strip() for seg in segments)
            return text or ""
        finally:
            try:
                os.unlink(tmp.name)
            except OSError:
                pass


asr_service = ASRService()
